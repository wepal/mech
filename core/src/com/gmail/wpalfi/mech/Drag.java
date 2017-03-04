package com.gmail.wpalfi.mech;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by wpalfi on 24.02.17.
 */

public class Drag {
    Vector2 startPix;
    Vector2 currentPix;
    Vector2 endPix;
    Vector3 startCamPosition;
    float startViewportWidth;
    Drawable startDrawable;
    Drawable endDrawable;
    DragType type=DragType.UNDEFINED;
}
