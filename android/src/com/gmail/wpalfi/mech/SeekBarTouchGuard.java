package com.gmail.wpalfi.mech;

import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

public class SeekBarTouchGuard implements SeekBar.OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                // Disallow Drawer to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                // Allow Drawer to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        // Handle seekbar touch events.
        v.onTouchEvent(event);
        return true;
    }
}
