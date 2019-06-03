package mkrainski.remoteclient;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    private static final long LEFT_CLICK_LIMIT = 1000;
    private static final long MESSAGE_DELAY = 100;
    private static final int MOUSE_MOVE_START_THRESHOLD = 2;
    private String host = null;
    private int port = 0;
    private final String TAG = MainActivity.class.getName();
    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private boolean move = false;
    private long actionStartTime = 0;
    private RemoteTextInput remoteTextInput;
    private TextView hostNameView;
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

        SharedPreferences sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE);

        host = sharedPref.getString(getString(R.string.saved_host), null);
        port = sharedPref.getInt(getString(R.string.saved_port), 0);

        remoteTextInput = findViewById(R.id.remote_text_input);
        hostNameView = findViewById(R.id.host_name);


        if (host!=null && port!=0){
            String hostPort = host + ":" + port;
            hostNameView.setText(hostPort);
        }

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
                switch(id) {
                    case R.id.enter_host_data:
                        enterHostDataAlert();
                        break;
                    default:
                        return true;
                }

                return true;

            }
        });
    }

    private void enterHostDataAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter hostname and port");

        final String inputTemplate = "^\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+$";

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected.
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input_text = input.getText().toString();
                if (!input_text.matches(inputTemplate)){
                    Toast.makeText(
                        MainActivity.this,
                        "Invalid input!",
                        Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                hostNameView.setText(input_text);

                host = input_text.split(":")[0];
                port = Integer.valueOf(input_text.split(":")[1]);

                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.saved_host), host);
                editor.putInt(getString(R.string.saved_port), port);
                editor.apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long now = System.currentTimeMillis();
        Log.d(TAG, "onTouchEvent: " + event.getAction());
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
                        SocketConnector.sendValue(
                            "move_mouse_relative: " + (x - lastX) + ", " + (y - lastY),
                            this,
                                host,
                                port
                        );
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
                        SocketConnector.sendValue("left_click", this, host, port);
                    else
                        SocketConnector.sendValue("right_click", this, host, port);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}
