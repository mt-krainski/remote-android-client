package mkrainski.remoteclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    private static final long LEFT_CLICK_LIMIT = 1000;
    private final String TAG = MainActivity.class.getName();
    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private boolean move = false;
    private long actionStartTime = 0;
    private EditText remoteTextInput;
    private int screenHeight;
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteTextInput = findViewById(R.id.remoteTextInput);

        remoteTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("")) {
                    sendValue("input("+s.toString()+")");
                    s.clear();
                }
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                move = false;
                actionStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((abs(lastX - x) > 10) || (abs(lastY - y) > 10)) {
                    sendValue("move_by(" + (lastX - x) + ", " + (lastY - y) + ")");
                    lastX = x;
                    lastY = y;
                    move = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!move) {
                    long now = System.currentTimeMillis();
                    if (now - actionStartTime < LEFT_CLICK_LIMIT)
                        sendValue("left_click()");
                    else
                        sendValue("right_click()");
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public boolean sendValue(String value){
        Log.d(TAG, "sendValue: "+ value);
        return true;
    }
}
