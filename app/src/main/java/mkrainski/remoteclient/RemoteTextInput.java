package mkrainski.remoteclient;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class RemoteTextInput extends FrameLayout {

    private static final String TAG = "RemoteTextInput";

    public RemoteTextInput(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public RemoteTextInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RemoteTextInput(Context context) {
        super(context);
        init();
    }
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        BaseInputConnection fic = new BaseInputConnection(this, false);
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_NULL;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return fic;
    }
    public void init(){
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "onKey: " + (char) event.getUnicodeChar());
                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        SocketConnector.sendValue("press_key: enter", getContext());
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                        SocketConnector.sendValue("press_key: backspace", getContext());
                    } else if (
                            keyCode == KeyEvent.KEYCODE_BACK ||
                            keyCode == KeyEvent.KEYCODE_HOME ||
                            keyCode == KeyEvent.KEYCODE_APP_SWITCH
                    ) {
                        Log.d(TAG, "onKey: Special");
                    } else {
                        SocketConnector.sendValue(
                            "text_input: " + (char) event.getUnicodeChar(),
                            getContext());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            boolean result = imm.showSoftInput(getRootView(), InputMethodManager.SHOW_FORCED);
            if (!result)
                imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
        return true;
    }
}
