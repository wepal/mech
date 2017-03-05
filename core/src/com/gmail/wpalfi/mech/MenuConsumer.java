package com.gmail.wpalfi.mech;

/**
 * Created by wpalfi on 19.02.17.
 */
public interface MenuConsumer {
    Properties getProperties();

    void setTool(Tool tool);

    void setColor(Color color);

    void setPause(boolean pause);
}
