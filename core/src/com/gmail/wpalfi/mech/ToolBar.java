package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import sun.security.pkcs11.wrapper.CK_SLOT_INFO;

public class ToolBar implements PropertiesListener, Slider.Listener {
    private Button _menu,_nodes,_edges,_slides,_radius,_color;
    private float _width=100;
    private Tool _tool;
    private PropertiesProvider _propertiesProvider;
    private Slider _slider;

    @Override
    public void propertiesChanged(Properties properties) {
        _tool=properties.tool;
    }

    @Override
    public void onSliderValueChanged(float value) {
        Properties properties = _propertiesProvider.getProperties();
        if(_property==Property.RADIUS){
            properties.radius=value;
        }
        _propertiesProvider.setProperties(properties);
    }

    private enum Property {
        RADIUS, COLOR
    }
    private Property _property;
    public ToolBar(PropertiesProvider propertiesProvider) {
        _propertiesProvider=propertiesProvider;
        _propertiesProvider.addPropertiesListener(this);
        Properties properties=_propertiesProvider.getProperties();
        _tool=properties.tool;

        _menu = new Button("menu.png","menu_check.png");
        _nodes = new Button("nodes.png", "nodes_check.png");
        _edges = new Button("edges.png", "edges_check.png");
        _slides = new Button("slide.png", "slide_check.png");
        _radius = new Button("radius.png", "radius_check.png");
        _color = new Button("color.png", "color_check.png");
    }

    public void render() {
        float y0 = Gdx.graphics.getHeight();
        _menu.render(y0-1*_width,_width,false);
        _nodes.render(y0-2*_width,_width, _tool==Tool.NODE);
        _edges.render(y0-3*_width,_width, _tool==Tool.EDGE);
        _slides.render(y0-4*_width,_width, _tool==Tool.SLIDE);
        _radius.render(y0-5*_width,_width, _property==Property.RADIUS);
        _color.render(y0-6*_width,_width, _property==Property.COLOR);
        if(_slider!=null){
            _slider.render();
        }
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button){
        if(_slider!=null) {
            if (_slider.touchDown(screenX, screenY,pointer,button)) {
                return true;
            }
            _slider = null;//lost "focus"
        }
        _property=null;
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
        if(index>=1 && index<=3){
            _tool = Tool.values()[index-1];
            Properties properties = _propertiesProvider.getProperties();
            properties.tool = _tool;
            _propertiesProvider.setProperties(properties);
            return true;
        }
        if(index>=4 && index<=5){
            setProperty(Property.values()[index-4]);
            return true;
        }
        return false;
    }

    private void setProperty(Property property){
        _property=property;
        Properties properties = _propertiesProvider.getProperties();
        float value = _property==Property.RADIUS ? properties.radius : 0;
        _slider=new Slider(value, this);
    }
}
