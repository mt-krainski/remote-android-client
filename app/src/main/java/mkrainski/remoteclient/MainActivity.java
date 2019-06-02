package mkrainski.remoteclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    private static final long LEFT_CLICK_LIMIT = 1000;
    private static final long MESSAGE_DELAY = 100;
    private static final int MOUSE_MOVE_START_THRESHOLD = 2;
    private final String TAG = MainActivity.class.getName();
    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private boolean move = false;
    private long actionStartTime = 0;
    private RemoteTextInput remoteTextInput;
    private int screenHeight;
    private int screenWidth;
    private long lastMessageSentAt=System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteTextInput = findViewById(R.id.remoteTextInput);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long now = System.currentTimeMillis();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                move = false;
                actionStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (
                    (abs(lastX - x) > MOUSE_MOVE_START_THRESHOLD) ||
                    (abs(lastY - y) > MOUSE_MOVE_START_THRESHOLD)
                ) {
                    if (now-lastMessageSentAt > MESSAGE_DELAY) {
                        sendValue("move_mouse_relative: " + (x - lastX) + ", " + (y - lastY));
                        lastX = x;
                        lastY = y;
                        lastMessageSentAt = now;
                    }
                    move = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!move) {
                    if (now - actionStartTime < LEFT_CLICK_LIMIT)
                        sendValue("left_click");
                    else
                        sendValue("right_click");
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public boolean sendValue(String value){
        Log.d(TAG, "sendValue: "+ value);
        Intent intent = new Intent(this, SocketConnector.class);
        intent.putExtra("message", value);
        startService(intent);
        return true;
    }
}
