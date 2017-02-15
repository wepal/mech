package com.gmail.wpalfi.mech;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;


public class NodesFragment extends PropertiesFragment {

    private SeekBar _radiusSeekBar;
    private SeekBar _strengthSeekBar;

    public NodesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nodes, container, false);

        FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.replace(R.id.colorsFragmentPlaceholder, new ColorsFragment());
        trans.commit();

        _radiusSeekBar = (SeekBar) view.findViewById(R.id.radiusSeekBar);
        _radiusSeekBar.setOnTouchListener(new SeekBarTouchGuard());
        _radiusSeekBar.setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!fromUser){
                    return;
                }
                Properties p=_propertiesProvider.getProperties();
                p.radius = 3f * progress / 100f;
                _propertiesProvider.setProperties(p);
            }
        });

        _strengthSeekBar = (SeekBar) view.findViewById(R.id.strengthSeekBar);
        _strengthSeekBar.setOnTouchListener(new SeekBarTouchGuard());
        _strengthSeekBar.setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //_interaction.setStrength(3f * progress / 100f);
            }
        });

        return view;
    }

    @Override
    public void propertiesChanged(Properties properties) {
        //_radiusSeekBar.setProgress((int)(properties.radius/3f*100f));
    }
}
