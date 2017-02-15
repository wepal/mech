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
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity
        implements AndroidFragmentApplication.Callbacks,NavigationDrawerFragment.NavigationDrawerCallbacks
        {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SceneFragment mSceneFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mSceneFragment = new SceneFragment();

        /*FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.container1, new StupidFragment());// AndroidLauncher.GameFragment());// mSceneFragment);
        trans.commit();
*/

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.container, mSceneFragment);
        trans.commit();
    }

    @Override
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
    }

    public void onSectionAttached(int number) {
    }

    @Override
    public void exit() {}
}
