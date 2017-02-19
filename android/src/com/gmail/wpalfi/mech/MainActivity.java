package com.gmail.wpalfi.mech;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends android.support.v4.app.FragmentActivity
        implements AndroidFragmentApplication.Callbacks
        {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SceneFragment mSceneFragment;

    protected void onCreate(Bundle savedInstanceState) {
        mSceneFragment = new SceneFragment();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.container, mSceneFragment);
        trans.commit();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    /*@Override
    public void onNavigationDrawerItemSelected(int position) {
        if(mSceneFragment!=null){
            switch(position){
                case 0:
                    mSceneFragment._mech.setTool(Tool.NODE);
                    break;
                case 1:
                    mSceneFragment._mech.setTool(Tool.EDGE);
                    break;
                case 2:
                    mSceneFragment._mech.togglePause();
                    break;
                case 3:
                    mSceneFragment._mech.toggleDebugRenderer();
                    break;
            }
        }
    }*/

    public PropertiesProvider getPropertiesProvider() {
        return mSceneFragment._mech;
    }

    @Override
    public void exit() {}
}
