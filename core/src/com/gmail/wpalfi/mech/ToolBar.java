package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import sun.security.pkcs11.wrapper.CK_SLOT_INFO;

public class ToolBar implements Slider.Listener, ColorMenu.Listener {
    private MenuConsumer _menuConsumer;
    private Button _menu,_nodes,_edges,_slides,_radius,_color,_delete,_pause;
    private float _width=60;
    private Slider _slider;
    private ColorMenu _colorMenu;

    @Override
    public void onSliderValueChanged(float value) {
        if(_property==Property.RADIUS){
            _menuConsumer.setRadius(value);
        }
    }

    @Override
    public void onColorMenuValueChanged(Color value) {
        _menuConsumer.setColor(value);
    }

    private enum Property {
        RADIUS, COLOR
    }
    private Property _property;
    public ToolBar(MenuConsumer menuConsumer) {
        _menuConsumer = menuConsumer;
        _menu = new Button("menu.png","menu.png");

        _nodes = new Button("nodes.png", "nodes_check.png");
        _edges = new Button("edges.png", "edges_check.png");
        _slides = new Button("slide.png", "slide_check.png");
        _delete = new Button("menu.png", "slide_check.png");

        _radius = new Button("radius.png", "radius_check.png");
        _color = new Button("color.png", "color_check.png");
        _pause = new Button("pause.png", "play.png");
    }

    public void render() {
        float y0 = Gdx.graphics.getHeight();
        Properties properties=_menuConsumer.getProperties();
        _menu.render(y0-1*_width,_width,false);
        _nodes.render(y0-2*_width,_width, properties.tool==Tool.NODE);
        _edges.render(y0-3*_width,_width, properties.tool==Tool.EDGE);
        _slides.render(y0-4*_width,_width, properties.tool==Tool.SLIDE);
        _delete.render(y0-5*_width,_width, properties.tool==Tool.DELETE);
        _radius.render(y0-6*_width,_width, _property==Property.RADIUS);
        _color.render(y0-7*_width,_width, _property==Property.COLOR);
        _pause.render(y0-8*_width,_width, properties.pause);
        if(_slider!=null){
            _slider.render();
        }
        if(_colorMenu!=null){
            _colorMenu.render();
        }
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button){
        if(_slider!=null) {
            if (_slider.touchDown(screenX, screenY,pointer,button)) {
                return true;
            }
        }
        if(_colorMenu!=null){
            if(_colorMenu.touchDown(screenX,screenY,pointer,button)){
                return true;
            }
        }
        _property=null;
        _slider = null;
        _colorMenu=null;
        return clicked(screenX,screenY);
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(_slider!=null) {
            return _slider.touchDragged(screenX,screenY,pointer);
        }
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(_slider!=null) {
            return _slider.touchUp(screenX,screenY,pointer,button);
        }
        return false;
    }

    private boolean clicked(int screenX, int screenY) {
        float y0 = Gdx.graphics.getHeight();
        int index = (int)(screenY/_width);
        if(screenX>_width){
            return false;
        }
        if(index>=1 && index<=4){
            Tool tool = Tool.values()[index-1];
            _menuConsumer.setTool(tool);
            return true;
        }
        if(index>=5 && index<=6){
            setProperty(Property.values()[index-5]);
            return true;
        }
        if(index==7){
            Properties properties = _menuConsumer.getProperties();
            _menuConsumer.setPause(!properties.pause);
        }
        return false;
    }

    private void setProperty(Property property){
        _property=property;
        Properties properties = _menuConsumer.getProperties();
        switch(property){
            case RADIUS:
                _slider=new Slider(properties.radius, this);
                break;
            case COLOR:
                _colorMenu=new ColorMenu(properties.color,this);
                break;
        }
    }
}
