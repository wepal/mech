package com.gmail.wpalfi.mech;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public final class Util {
    public static void setRendererColor(ShapeRenderer renderer, Color color){
        switch(color) {
            case WHITE:
                renderer.setColor(1, 1, 1, 1);
                return;
            case YELLOW:
                renderer.setColor(1, 1, 0, 1);
                return;
            case GREEN:
                renderer.setColor(0, 1, 0, 1);
                return;
        }
    }
    public static short categoryBits(Color color){
        return (short)(0x0001 << color.ordinal());
    }
    public static short maskBits(Color color){
        switch(color) {
            case WHITE:
                return 0x0000;
            case YELLOW:
                return categoryBits(Color.YELLOW);
            case GREEN:
                return categoryBits(Color.GREEN);
        }
        return 0x0000;
    }
    public static Vector2 unproject(OrthographicCamera camera, Vector2 pix){
        Vector3 vector3 = new Vector3(pix.x,pix.y,0);
        camera.unproject(vector3);
        Vector2 vector2 = new Vector2(vector3.x,vector3.y);
        return vector2;
    }
}
