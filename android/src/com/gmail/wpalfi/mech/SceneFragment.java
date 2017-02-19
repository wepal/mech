package com.gmail.wpalfi.mech;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class SceneFragment extends AndroidFragmentApplication
{
    public Mech _mech;

    public SceneFragment(){
        _mech=new Mech();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return initializeForView(_mech);
    }
}
