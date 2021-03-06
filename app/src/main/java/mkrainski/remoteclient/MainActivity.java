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
    private static int message_delay_ms = 10;
    private static final int MOUSE_MOVE_START_THRESHOLD = 5;
    private String host = null;
    private int port = 0;
    private final String TAG = MainActivity.class.getName();
    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private static final float X_MUL = 2.0f;
    private static final float Y_MUL = 2.0f;
    private static final float SCROLL_MUL = -1.0f;
    private static final float SCREEN_SCROLL_LIMIT_RATIO = 0.85f;
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
    private boolean scroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE);

        try {
            host = sharedPref.getString(getString(R.string.saved_host), null);
            port = sharedPref.getInt(getString(R.string.saved_port), 0);
            message_delay_ms = sharedPref.getInt(getString(R.string.saved_delay_ms), message_delay_ms);
        } catch (ClassCastException e){
            Log.e(TAG, "onCreate: ", e);
        }


        remoteTextInput = findViewById(R.id.remote_text_input);
        hostNameView = findViewById(R.id.host_name);


        if (host!=null && port!=0){
            String hostPort = host + ":" + port;
            hostNameView.setText(hostPort);
            SocketConnector.init(host, port);
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

        remoteTextInput.requestFocus();

        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id) {
                    case R.id.enter_host_data:
                        enterHostDataAlert();
                        break;
                    case R.id.enter_delay:
                        enterDelayAlert();
                        break;
                    default:
                        return true;
                }

                return true;

            }
        });
    }

    private void enterHostDataAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle("Enter hostname and port");

        final String ipInputTemplate =
                "^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|(\\d{1,3}\\.){3}\\d{1,3}:\\d+$";

        // Set up the input
        final EditText input = new EditText(this);

        input.setText(host + ":" + port);
        // Specify the type of input expected.
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input_text = input.getText().toString();
                if (!input_text.matches(ipInputTemplate)){
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

    private void enterDelayAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle("Enter delay between commands [ms]");

        final String inputTemplate = "^\\d+$";

        // Set up the input
        final EditText input = new EditText(this);

        input.setText(String.valueOf(message_delay_ms));
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
                            "Please enter a valid integer.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                try {
                    message_delay_ms = Integer.valueOf(input_text);
                } catch (ClassCastException e){
                    Toast.makeText(
                            MainActivity.this,
                            "Please enter a valid integer.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.saved_delay_ms), message_delay_ms);
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
    protected void onPause() {
        super.onPause();
        SocketConnector.closeConnection();
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
                if (x > screenWidth * SCREEN_SCROLL_LIMIT_RATIO) {
                    scroll = true;
                }
                move = false;
                actionStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (
                        (abs(lastX - x) > MOUSE_MOVE_START_THRESHOLD) ||
                        (abs(lastY - y) > MOUSE_MOVE_START_THRESHOLD) ||
                        move
                ) {
                    if (now-lastMessageSentAt > message_delay_ms) {
                        if (scroll)
                            SocketConnector.sendValue(
                                    "scroll_mouse: " +
                                            SCROLL_MUL*(y - lastY),
                                    this,
                                    host,
                                    port
                            );
                        else
                            SocketConnector.sendValue(
                                "move_mouse_relative: " +
                                        X_MUL*(x - lastX) + ", " +
                                        Y_MUL*(y - lastY),
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
                    if (scroll) {
                        SocketConnector.sendValue("middle_click", this, host, port);
                    } else {
                        if (now - actionStartTime < LEFT_CLICK_LIMIT)
                            SocketConnector.sendValue("left_click", this, host, port);
                        else
                            SocketConnector.sendValue("right_click", this, host, port);
                    }
                }
                scroll = false;
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
