package com.gmail.wpalfi.mech;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class SceneFragment extends AndroidFragmentApplication
{
    public Mech _mech;

    // 5. Add the initializeForView() code in the Fragment's onCreateView method.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _mech = new Mech();
        return initializeForView(_mech);
    }
}
