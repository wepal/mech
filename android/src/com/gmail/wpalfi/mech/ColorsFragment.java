package com.gmail.wpalfi.mech;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class ColorsFragment extends PropertiesFragment implements OnClickListener{

    public ColorsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_colors, container, false);

        Button button = (Button) view.findViewById(R.id.button_white);
        button.setOnClickListener(this);
        button = (Button) view.findViewById(R.id.button_yellow);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Properties p=_propertiesProvider.getProperties();
        switch(v.getId()) {
            case R.id.button_white:
                p.color=Color.WHITE;
                break;
            case R.id.button_yellow:
                p.color=Color.YELLOW;
        }
        _propertiesProvider.setProperties(p);
    }

}
