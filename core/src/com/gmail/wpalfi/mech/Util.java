package com.gmail.wpalfi.mech;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Util {
    public static void setRendererColor(ShapeRenderer renderer, Color color){
        if(color==Color.WHITE)
            renderer.setColor(1, 1, 1, 1);
        else
            renderer.setColor(1, 1, 0, 1);
    }
}
