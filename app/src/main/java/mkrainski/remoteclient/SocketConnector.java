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
    private static String newHost = null;
    private static int newPort = 0;
    private static String connectedHost = null;
    private static int connectedPort = 0;

    public SocketConnector(){
        super("SocketConnector");
    }

    public static void init(String host, int port){
        newHost = host;
        newPort = port;
    }

    public static boolean startConnection(String host, int port){
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
        connectedHost = host;
        connectedPort = port;
        newHost = null;
        newPort = 0;
        Log.i(TAG, "startConnection: Connection established");
        return true;
    }

    public static void closeConnection(){
        if (connectionSocket != null) {
            try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectionSocket = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");

        if (connectionSocket==null && newHost!=null && newPort!=0) {
            startConnection(newHost, newPort);
        }

        if (newHost!=null && newPort!=0) {
            if (!newHost.equals(connectedHost) || newPort != connectedPort) {
                closeConnection();
                startConnection(newHost, newPort);
            }
        }

        if (connectionSocket==null)
            return;

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
                closeConnection();
            }
        }
    }

    public static boolean sendValue(String value, Context context) {
        Log.d(TAG, "sendValue: "+ value);
        Intent intent = new Intent(context, SocketConnector.class);
        intent.putExtra("message", value);
        context.startService(intent);
        return true;
    }

    public static boolean sendValue(String value, Context context, String host, int port) {
        newHost = host;
        newPort = port;
        return sendValue(value, context);
    }
}
