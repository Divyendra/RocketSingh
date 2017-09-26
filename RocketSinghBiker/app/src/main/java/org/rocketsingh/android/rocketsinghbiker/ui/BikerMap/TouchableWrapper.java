package org.rocketsingh.android.rocketsinghbiker.ui.BikerMap;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Divyu on 10/2/2015.
 * This layout is a hack to detect motion events like drag on map which is normally not available.
 */
public  class TouchableWrapper extends FrameLayout {

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds.
    private UpdateMapAfterUserInterection updateMapAfterUserInterection;

    public TouchableWrapper(Context context) {
        super(context);
        // Force the host activity to implement the UpdateMapAfterUserInterection Interface
        try {
            updateMapAfterUserInterection = (UpdateMapAfterUserInterection)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInterection");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouched = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                final long now = System.currentTimeMillis();
                if (now - lastTouched > SCROLL_TIME) {
                    // Update the map
                    updateMapAfterUserInterection.onUpdateMapAfterUserInterection();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInterection {
        public void onUpdateMapAfterUserInterection();
    }
}
