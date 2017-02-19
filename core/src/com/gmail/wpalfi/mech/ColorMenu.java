package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wpalfi on 19.02.17.
 */

public class ColorMenu {
    private float _backgroundX,_backgroundY,_backgroundWidth,_backgroundHeight;
    private Color _value;
    private List<Color> _colors = new ArrayList<Color>();
    private float _buttonWidth = 50;
    private float _padding = 30;
    private float _margin = 25;
    private ShapeRenderer _renderer;
    private Listener _listener;

    public ColorMenu(Color value, Listener listener){
        _value=value;
        _listener=listener;
        _renderer = new ShapeRenderer();
        _colors.add(Color.WHITE);
        _colors.add(Color.YELLOW);
        float w = Gdx.graphics.getWidth();
        _backgroundWidth=_colors.size()*_buttonWidth+(_colors.size()-1)*_padding + 2*_margin;
        _backgroundX=w/2-_backgroundWidth/2;
        _backgroundY=50;
        _backgroundHeight=_buttonWidth+2*_margin;
    }
    public void render(){
        _renderer.begin(ShapeRenderer.ShapeType.Filled);
        _renderer.setColor(1, 1, 1, 1);
        _renderer.rect(_backgroundX,_backgroundY,_backgroundWidth,_backgroundHeight);
        for(int i=0;i<_colors.size();i++){
            if(_colors.get(i)==_value) {
                _renderer.setColor(.3f,.3f,.3f,1);
            }else{
                _renderer.setColor(.7f,.7f,.7f,1);
            }
            float x=_backgroundX + _margin + i*(_buttonWidth+_padding);
            float y=_backgroundY+_backgroundHeight/2-_buttonWidth/2;
            _renderer.rect(x-5,y-5,_buttonWidth+10,_buttonWidth+10);
            ColorUtil.setRendererColor(_renderer,_colors.get(i));
            _renderer.rect(x,y,_buttonWidth,_buttonWidth);
        }
        _renderer.end();
    }
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float y = Gdx.graphics.getHeight() - screenY;
        if (screenX < _backgroundX
                || screenX > _backgroundX + _backgroundWidth
                || y < _backgroundY
                || y > _backgroundY + _backgroundHeight) {
            return false;
        }
        float x0 = _backgroundX + _margin - _padding/2;
        int x = (int)((screenX - x0) / (_buttonWidth+_padding));
        if (x < 0) {
            x = 0;
        }
        if (x >= _colors.size()) {
            x = _colors.size()-1;
        }
        _value = Color.values()[x];
        _listener.onColorMenuValueChanged(_value);
        return true;
    }

    public interface Listener{
        public void onColorMenuValueChanged(Color value);
    }
}
