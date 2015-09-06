package com.opteam.virtualight;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class View3DRenderer {
    private static final String TAG = "VIEW3DRENDERER";

    private Context mContext;
    private RelativeLayout leftOverlayLayout;
    private RelativeLayout rightOverlayLayout;

    View3DRenderer(Context context, LinearLayout overlayLayout) {
        mContext = context;

        leftOverlayLayout = (RelativeLayout) overlayLayout.findViewById(R.id.left_overlay);
        rightOverlayLayout = (RelativeLayout) overlayLayout.findViewById(R.id.right_overlay);
    }

    HashMap<String, View[]> viewHashMap = new HashMap<>();

//    public View getView(String tag) {
//        return viewHashMap.get(tag)[0];
//    }

    public void addTextView(String tag, TextView leftTextView) {
        TextView rightTextView = new TextView(mContext);

        copyTextView(leftTextView, rightTextView);

        leftOverlayLayout.addView(leftTextView);
        rightOverlayLayout.addView(rightTextView);

        View[] views = {leftTextView, rightTextView};
        viewHashMap.put(tag, views);
    }

    private void copyTextView(TextView leftTextView, TextView rightTextView) {
        // Add other copy methods as needed...
        rightTextView.setTextColor(leftTextView.getCurrentTextColor());
        rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, leftTextView.getTextSize());
        rightTextView.setText(leftTextView.getText());
        rightTextView.setGravity(leftTextView.getGravity());
        rightTextView.setLayoutParams(leftTextView.getLayoutParams());
    }

    public void updateTextView(String tag, TextView leftTextView) {
        TextView rightTextView = (TextView) viewHashMap.get(tag)[1];
        copyTextView(leftTextView, rightTextView);
    }

}
