package com.gmail.wpalfi.mech;

public interface PropertiesProvider {
    public Properties getProperties();
    public void setProperties(Properties properties);
    public void addPropertiesListener(PropertiesListener propertiesListener);
    public void removePropertiesListener(PropertiesListener propertiesListener);
}
