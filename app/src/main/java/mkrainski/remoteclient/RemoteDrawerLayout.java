package mkrainski.remoteclient;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;

public class RemoteDrawerLayout extends DrawerLayout {

    private boolean isSwipeOpenEnabled = false;
    private final static String TAG = "RemoteDrawerLayout";

    public RemoteDrawerLayout(@NonNull Context context) {
        super(context);
    }

    public RemoteDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSwipeOpenEnabled && !isDrawerVisible(Gravity.START)){
            Log.d(TAG, "onInterceptTouchEvent: false");
            return false;
        }
        boolean retValue = super.onInterceptTouchEvent(ev);
        Log.d(TAG, "onInterceptTouchEvent: super = " + retValue);
        return retValue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isSwipeOpenEnabled && !isDrawerVisible(Gravity.START)){
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
