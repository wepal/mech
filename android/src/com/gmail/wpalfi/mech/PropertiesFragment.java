package com.gmail.wpalfi.mech;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;


public class PropertiesFragment extends Fragment implements PropertiesListener{

    protected PropertiesProvider _propertiesProvider;

    public PropertiesFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PropertiesProvider) {
            _propertiesProvider = (PropertiesProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PropertiesProvider");
        }
        _propertiesProvider.addPropertiesListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _propertiesProvider.removePropertiesListener(this);
        _propertiesProvider = null;
    }

    @Override
    public void propertiesChanged(Properties properties) {

    }
}
