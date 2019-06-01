package mkrainski.remoteclient;

import android.app.IntentService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
    private EditText remoteTextInput;
    private int screenHeight;
    private int screenWidth;
    private long lastMessageSentAt=System.currentTimeMillis();

    public static class SocketConnector extends IntentService {

        private final static String TAG = SocketConnector.class.getName();
        private static Socket connectionSocket = null;
        private static PrintWriter connectionOutput;
        private static BufferedReader connectionInput;

        public SocketConnector(){
            super("SocketConnector");
        }

        public boolean startConnection(String host, int port){
            try {
                connectionSocket = new Socket(host, port);
                connectionOutput = new PrintWriter(
                        connectionSocket.getOutputStream(), true);
                connectionInput = new BufferedReader(
                        new InputStreamReader(connectionSocket.getInputStream()));
            } catch (UnknownHostException e){
                Log.e(TAG, "startConnection: unknown host: " + host);
                return false;
            } catch (IOException e) {
                Log.e(TAG, "startConnection: I/O Exception.");
                e.printStackTrace();
                return false;
            }
            Log.i(TAG, "startConnection: Connection established");
            return true;
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent: ");
            if (connectionSocket == null) {
                startConnection("192.168.0.20", 9201);
            }

            if (intent==null)
                return;

            String message = intent.getStringExtra("message");
            if (connectionSocket != null) {
                try {
                    connectionOutput.println(message);
                    String received = connectionInput.readLine();
                    if (received!=null)
                        Log.d(TAG, "received: " + received);
                    else
                        connectionSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
                String text = s.toString();
                if(!text.equals("")) {
                    sendValue("text_input: "+ text.substring(text.length()-1));
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
