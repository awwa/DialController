package com.awwa.sample.dialcontrollersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.awwa.widget.DialController;
import com.awwa.widget.DialController.OnOperationListener;
import com.awwa.widget.DialController.Operate;

public class MainActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();
    private final MainActivity self = this;

    private TextView mText;
    private DialController mDial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.text);
        mDial = (DialController) findViewById(R.id.controller);
        mDial.setOnOperationListener(new OnOperationListener() {

            @Override
            public void onOperation(Operate operate) {
                mText.setText(operate.toString());
                if (operate == Operate.ENTER) {
                    startActivity(new Intent(self, SubActivity.class));
                }
            }
        });
    }
}
