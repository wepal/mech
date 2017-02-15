package com.gmail.wpalfi.mech;

/**
 * Created by wpalfi on 07.02.17.
 */

import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;

public class Pull {
    float homeX,homeY;
    List<Drive> drives = new ArrayList<Drive>();
    Body body;
    Body homeBody;
    DistanceJoint pullBackJoint;
}
