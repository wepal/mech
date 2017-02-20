package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by wpalfi on 19.02.17.
 */

public class Slider {
    private float _backgroundX,_backgroundY,_backgroundWidth,_backgroundHeight;
    private float _x0,_x1,_y,_value;
    private ShapeRenderer _renderer;
    private Listener _listener;
    private boolean _dragging=false;
    private int _pointer;
    public Slider(float value, Listener listener){
        _value=value;
        _listener=listener;
        _renderer = new ShapeRenderer();
        float w = Gdx.graphics.getWidth();
        _x0=w/4;
        _x1=3*w/4;
        _y=100;
        _backgroundX=_x0-50;
        _backgroundY=50;
        _backgroundWidth=_x1-_x0+100;
        _backgroundHeight=100;
    }
    public void render(){
        _renderer.begin(ShapeRenderer.ShapeType.Filled);
        _renderer.setColor(1, 1, 1, 1);
        _renderer.rect(_backgroundX,_backgroundY,_backgroundWidth,_backgroundHeight);
        _renderer.setColor(.7f,.7f,.7f, 1);
        _renderer.rect(_x0,_y-4,_x1-_x0,8);
        _renderer.setColor(63/255f,81/255f,181/255f, 1);
        float x = _x0*(1-_value)+_x1*_value;
        _renderer.rect(_x0,_y-4,x-_x0,8);
        _renderer.circle(x, _y, _dragging?40:26, 64);
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
        _dragging = true;
        _pointer=pointer;
        handleTouch(screenX,screenY);
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(!_dragging || pointer!=_pointer){
            return false;
        }
        handleTouch(screenX,screenY);
        return true;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(!_dragging || pointer!=_pointer){
            return false;
        }
        _dragging=false;
        handleTouch(screenX,screenY);
        return true;
    }

    private void handleTouch(int screenX, int screenY) {
        float x = (screenX - _x0) / (_x1 - _x0);
        if (x < 0) {
            x = 0;
        }
        if (x > 1) {
            x = 1;
        }
        _value = x;
        _listener.onSliderValueChanged(_value);
    }

    public interface Listener{
        public void onSliderValueChanged(float value);
    }
}
