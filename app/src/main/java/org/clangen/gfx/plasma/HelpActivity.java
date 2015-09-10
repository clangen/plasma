package org.clangen.gfx.plasma;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

public class HelpActivity extends Activity
{
    private Plasma mPlasma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.help);

        mPlasma = Plasma.getInstance(this);

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.SurfaceView);
        surfaceView.getHolder().addCallback(mSurfaceHolderCallback);

        findViewById(R.id.OpenPickerButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
                    }
                });

        findViewById(R.id.SettingsButton).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HelpActivity.this, SettingsActivity.class));
                }
            });

        findViewById(R.id.CloseButton).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        public void surfaceDestroyed(SurfaceHolder holder) {
            mPlasma.stop(holder);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // surfaceChanged() always called at least once after created(), start it there.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface().isValid()) {
                mPlasma.start(holder);
            }
        }
    };
}
