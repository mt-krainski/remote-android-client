package mkrainski.remoteclient;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

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
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteTextInput = findViewById(R.id.remoteTextInput);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        dl = findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        nv = findViewById(R.id.nv);

        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.account:
                        Toast.makeText(MainActivity.this, "My Account",Toast.LENGTH_SHORT).show();break;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, "Settings",Toast.LENGTH_SHORT).show();break;
                    case R.id.mycart:
                        Toast.makeText(MainActivity.this, "My Cart",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }


                return true;

            }
        });
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}
