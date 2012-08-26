package org.clangen.gfx.plasma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class PlasmaService extends WallpaperService {

    private SharedPreferences mPrefs;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private class Engine extends WallpaperService.Engine {
        private Plasma mPlasma;
        private boolean mVisible;

        public Engine() {
            mVisible = false;
            mPlasma = Plasma.getInstance(PlasmaService.this);
            updatePlasmaThreadPriority();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            registerReceivers();
        }

        @Override
        public void onDestroy() {
            unregisterReceivers();
            super.onDestroy();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            if (mVisible && ( ! SettingsActivity.isActive())) {
                start();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            mVisible = visible;

            if (visible) {
                if ( ! SettingsActivity.isActive()) {
                    start();
                }
            }
            else {
                stop();
            }
        }

        private synchronized void start() {
            mPlasma.start(getSurfaceHolder());
        }

        private synchronized void stop() {
            mPlasma.stop(getSurfaceHolder());
        }

        private void registerReceivers() {
            registerReceiver(
                mOnSettingsFinishedReceiver,
                new IntentFilter(SettingsActivity.ACTION_SETTINGS_FINISHED));

            registerReceiver(
                mOnEffectChangedReceiver,
                new IntentFilter(Effect.ACTION_EFFECT_CHANGED));

            registerReceiver(
                mOnSettingsPriorityChangedReceiver,
                new IntentFilter(SettingsActivity.ACTION_SETTINGS_PRIORITY_CHANGED));
        }

        private void unregisterReceivers() {
            unregisterReceiver(mOnSettingsFinishedReceiver);
            unregisterReceiver(mOnEffectChangedReceiver);
            unregisterReceiver(mOnSettingsPriorityChangedReceiver);
        }

        private void updatePlasmaThreadPriority() {
            boolean lowPriority =
                mPrefs.getBoolean(getString(R.string.pref_low_priority), true);

            Plasma.setThreadPriority(
                lowPriority ? Thread.MIN_PRIORITY : Thread.NORM_PRIORITY);
        }

        private BroadcastReceiver mOnEffectChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Plasma.onEffectChanged();
            }
        };

        private BroadcastReceiver mOnSettingsFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mVisible) {
                    start();
                }
            }
        };

        private BroadcastReceiver mOnSettingsPriorityChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePlasmaThreadPriority();
            }
        };
    }
}
