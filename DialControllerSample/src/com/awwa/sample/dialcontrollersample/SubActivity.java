package com.awwa.sample.dialcontrollersample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.awwa.widget.DialController;
import com.awwa.widget.DialController.OnOperationListener;
import com.awwa.widget.DialController.Operate;

public class SubActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = SubActivity.class.getSimpleName();
    private final SubActivity self = this;

    private TextView mText;
    private DialController mDial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        mText = (TextView) findViewById(R.id.text);
        mDial = (DialController) findViewById(R.id.controller);
        mDial.setOnOperationListener(new OnOperationListener() {
            @Override
            public void onOperation(Operate operate) {
                mText.setText(operate.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.change_text_up:
            mDial.setTextUp("new up");
            break;
        case R.id.change_text_down:
            mDial.setTextDown("new down");
            break;
        case R.id.change_text_left:
            mDial.setTextLeft("new left");
            break;
        case R.id.change_text_right:
            mDial.setTextRight("new right");
            break;
        case R.id.change_text_enter:
            mDial.setTextEnter("new enter");
            break;
        case R.id.change_text_size:
            mDial.setTextSize(36f);
            break;
        case R.id.change_text_color:
            mDial.setTextColor(Color.RED);
            break;
        case R.id.toggle_vibrate:
            boolean enableVibrate = mDial.getEnableVibrate();
            mDial.setEnableVibrate(!enableVibrate);
            break;
        case R.id.change_bg_outer_ring:
            Drawable bgOuterRing = self.getResources().getDrawable(
                    R.drawable.new_bg_outer_ring);
            mDial.setBgOuterRing(bgOuterRing);
            break;
        case R.id.change_bg_inner_ring:
            Drawable bgInnerRing = self.getResources().getDrawable(
                    R.drawable.new_bg_inner_ring);
            mDial.setBgInnerRing(bgInnerRing);
            break;
        case R.id.change_bg_marker:
            Drawable bgMarker = self.getResources().getDrawable(
                    R.drawable.new_bg_marker);
            mDial.setBgMarker(bgMarker);
            break;
        }
        return true;
    }
}
