package mkrainski.remoteclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();
    private float lastX = 0.0f;
    private float lastY = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "onTouchEvent: " + x + ", " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                Log.d(TAG, "onTouchEvent: ActionDown");
                break;
            case MotionEvent.ACTION_MOVE:

                Log.d(TAG, "onTouchEvent: ActionMove");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ActionUp");
                break;
        }
        return super.onTouchEvent(event);
    }
}
