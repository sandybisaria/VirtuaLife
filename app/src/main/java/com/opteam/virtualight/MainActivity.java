package com.opteam.virtualight;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "MAINACTIVITY";

    private static final UUID PEBBLE_UUID = UUID.fromString("03c9f6cc-e9e4-4697-893f-90ecc16aa768");
    private boolean isAppLaunched = false;

    private Camera mCamera;
    private boolean isOpen = false;

    private TextureView leftView;
    private ImageView rightView;

    private View3DRenderer renderer;

    private final String statusTag = "STATUS";
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        leftView = (TextureView) findViewById(R.id.camera_preview_left);
        rightView = (ImageView) findViewById(R.id.camera_preview_right);

        leftView.setSurfaceTextureListener(this);

        renderer = new View3DRenderer(this,
                ((LinearLayout) findViewById(R.id.overlay_layer_layout)));

        statusView = new TextView(this);
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(10);
        statusView.setText(getPebbleStatusMessage());
        statusView.setGravity(Gravity.CENTER_HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        statusView.setLayoutParams(params);
        renderer.addTextView(statusTag, statusView);
        BroadcastReceiver statusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                statusView.setText(getPebbleStatusMessage());
                renderer.updateTextView(statusTag, statusView);
            }

        };

        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), statusReceiver);
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), statusReceiver);
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_UUID);
    }

    private String getPebbleStatusMessage() {
        boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
        return "Status: " + (connected ? "Connected" : "Not connected");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (!isOpen) {
            mCamera = Camera.open();
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            int[] bestFps = parameters.getSupportedPreviewFpsRange().get(1);
            parameters.setPreviewFpsRange(bestFps[0], bestFps[1]);

            int sidePadding = 100;
            int oldWidth = leftView.getWidth();
            int newWidth = oldWidth - sidePadding;
            int oldHeight = leftView.getHeight();
            // 1440 width, 1080 height
            Camera.Size bestSize = parameters.getSupportedPreviewSizes().get(1);
            int newHeight = (int) (newWidth * (bestSize.height * 1.0) / bestSize.width);
            int padding = (oldHeight - newHeight) / 2;

            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_activity_layout);
            mainLayout.setPadding(sidePadding, padding, sidePadding, padding);

            parameters.setPreviewSize(bestSize.width, bestSize.height);

            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            // Something bad happened
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        isOpen = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
        Bitmap image = leftView.getBitmap();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image);
        rightView.setImageDrawable(bitmapDrawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
