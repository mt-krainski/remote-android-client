package mkrainski.remoteclient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnector extends IntentService {

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

    public static boolean sendValue(String value, Context context){
        Log.d(TAG, "sendValue: "+ value);
        Intent intent = new Intent(context, SocketConnector.class);
        intent.putExtra("message", value);
        context.startService(intent);
        return true;
    }
}
