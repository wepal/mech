package com.gmail.wpalfi.mech;

/**
 * Created by wpalfi on 15.02.17.
 */

public class Properties {
    float radius=.3f;
    Color color=Color.WHITE;
    Tool tool=Tool.NODE;
    boolean pause=false;
    boolean useDebugRenderer=false;

    public Properties(){
    }

    public Properties(Properties p){
        radius=p.radius;
        color=p.color;
        tool=p.tool;
        pause=p.pause;
        useDebugRenderer=p.useDebugRenderer;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof Properties)){
            return false;
        }
        Properties p=(Properties)object;
        return radius==p.radius
                && color==p.color
                && tool==p.tool
                && pause==p.pause
                && useDebugRenderer==p.useDebugRenderer;
    }
}
