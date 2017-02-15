package com.gmail.wpalfi.mech;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.FragmentTabHost;
//import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.TabHost;


public class NavigationDrawerFragment extends PropertiesFragment implements TabHost.OnTabChangeListener{

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private FragmentTabHost mTabHost;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.drawer_main,container, false);

        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("nodes").setIndicator("Nodes"),
                NodesFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("edges").setIndicator("Edges"),
                StupidFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("fragmentd").setIndicator("Fragment D"),
                StupidFragment.class, null);
        mTabHost.setOnTabChangedListener(this);

        return rootView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setScrimColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listene
    }

    @Override
    public void onTabChanged(String tabId) {
        Properties p=_propertiesProvider.getProperties();
        switch(tabId){
            case "nodes":
                p.tool=Tool.NODE;
                break;
            case "edges":
                p.tool=Tool.EDGE;
                break;
        }
        _propertiesProvider.setProperties(p);
    }

}
