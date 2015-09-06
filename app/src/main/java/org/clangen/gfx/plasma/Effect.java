package org.clangen.gfx.plasma;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Effect {
    public static final String ACTION_EFFECT_CHANGED = "org.clangen.gfx.plasma.ACTION_SETTINGS_CHANGED";

    private final static int R_AMOUNT = 181;
    private final static int R_INTENSITY = 33;
    private final static int R_WAVELENGTH = 140;
    private final static int G_AMOUNT = 151;
    private final static int G_INTENSITY = 31;
    private final static int G_WAVELENGTH = 92;
    private final static int B_AMOUNT = 125;
    private final static int B_INTENSITY = 27;
    private final static int B_WAVELENGTH = 120;
    private final static int SIZE = 1;
    private final static int SPEED1 = 3;
    private final static int SPEED2 = 1;
    private final static int SPEED3 = 8;
    private final static int SPEED4 = 1;

    private static Effect sInstance;

    private SharedPreferences mPrefs;
    private Application mContext;
    private boolean mTransaction;

    private Effect(Context context) {
        mContext = (Application) context.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static Effect getInstance(Context context) {
        synchronized (Effect.class) {
            if (sInstance == null) {
                sInstance = new Effect(context.getApplicationContext());
            }

            return sInstance;
        }
    }

    public int getSize() {
        return Math.max(1, getInteger(R.string.pref_size, SIZE));
    }

    public void setSize(int size) {
        setInteger(R.string.pref_size, size);
    }

    public int getXPulseSpeed() {
        return getInteger(R.string.pref_speed1, SPEED1);
    }

    public void setXPulseSpeed(int speed) {
        setInteger(R.string.pref_speed1, speed);
    }

    public int getXMoveSpeed() {
        return getInteger(R.string.pref_speed2, SPEED2);
    }

    public void setXMoveSpeed(int speed) {
        setInteger(R.string.pref_speed2, speed);
    }

    public int getYPulseSpeed() {
        return getInteger(R.string.pref_speed3, SPEED3);
    }

    public void setYPulseSpeed(int speed) {
        setInteger(R.string.pref_speed3, speed);
    }

    public int getYMoveSpeed() {
        return getInteger(R.string.pref_speed4, SPEED4);
    }

    public void setYMoveSpeed(int speed) {
        setInteger(R.string.pref_speed4, speed);
    }

    public int getRedAmount() {
        return getInteger(R.string.pref_red_amount, R_AMOUNT);
    }

    public void setRedAmount(int value) {
        setInteger(R.string.pref_red_amount, value);
    }

    public int getRedIntensity() {
        return getInteger(R.string.pref_red_intensity, R_INTENSITY);
    }

    public void setRedIntensity(int value) {
        setInteger(R.string.pref_red_intensity, value);
    }

    public int getRedWavelength() {
        return getInteger(R.string.pref_red_wavelength, R_WAVELENGTH);
    }

    public void setRedWavelength(int value) {
        setInteger(R.string.pref_red_wavelength, value);
    }

    public int getGreenAmount() {
        return getInteger(R.string.pref_green_amount, G_AMOUNT);
    }

    public void setGreenAmount(int value) {
        setInteger(R.string.pref_green_amount, value);
    }

    public int getGreenIntensity() {
        return getInteger(R.string.pref_green_intensity, G_INTENSITY);
    }

    public void setGreenIntensity(int value) {
        setInteger(R.string.pref_green_intensity, value);
    }

    public int getGreenWavelength() {
        return getInteger(R.string.pref_green_wavelength, G_WAVELENGTH);
    }

    public void setGreenWavelength(int value) {
        setInteger(R.string.pref_green_wavelength, value);
    }

    public int getBlueAmount() {
        return getInteger(R.string.pref_blue_amount, B_AMOUNT);
    }

    public void setBlueAmount(int value) {
        setInteger(R.string.pref_blue_amount, value);
    }

    public int getBlueIntensity() {
        return getInteger(R.string.pref_blue_intensity, B_INTENSITY);
    }

    public void setBlueIntensity(int value) {
        setInteger(R.string.pref_blue_intensity, value);
    }

    public int getBlueWavelength() {
        return getInteger(R.string.pref_blue_wavelength, B_WAVELENGTH);
    }

    public void setBlueWavelength(int value) {
        setInteger(R.string.pref_blue_wavelength, value);
    }

    public void resetEffectToDefault() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(getKey(R.string.pref_size));
        editor.remove(getKey(R.string.pref_red_amount));
        editor.remove(getKey(R.string.pref_red_intensity));
        editor.remove(getKey(R.string.pref_red_wavelength));
        editor.remove(getKey(R.string.pref_green_amount));
        editor.remove(getKey(R.string.pref_green_intensity));
        editor.remove(getKey(R.string.pref_green_wavelength));
        editor.remove(getKey(R.string.pref_blue_amount));
        editor.remove(getKey(R.string.pref_blue_intensity));
        editor.remove(getKey(R.string.pref_blue_wavelength));
        editor.remove(getKey(R.string.pref_speed1));
        editor.remove(getKey(R.string.pref_speed2));
        editor.remove(getKey(R.string.pref_speed3));
        editor.remove(getKey(R.string.pref_speed4));
        editor.commit();

        broadcastChanged();
    }

    private String getKey(int key) {
        return mContext.getString(key);
    }

    private int getInteger(int key, int defValue) {
        return mPrefs.getInt(getKey(key), defValue);
    }

    private void setInteger(int key, int value) {
        if (setIntegerNoBroadcast(key, value)) {
            if ( ! mTransaction) {
                broadcastChanged();
            }
        }
    }

    private boolean setIntegerNoBroadcast(int key, int value) {
        int current = getInteger(key, -1);
        if (current != value) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(getKey(key), value);
            editor.commit();
            return true;
        }

        return false;
    }

    private synchronized void beginTransaction() {
        endTransaction();
        mTransaction = true;
    }

    private synchronized void endTransaction() {
        if (mTransaction) {
            mTransaction = false;
            broadcastChanged();
        }
    }

    public String toString() {
        JSONObject json = new JSONObject();

        try {
            json.put(getKey(R.string.pref_size), getSize());
            json.put(getKey(R.string.pref_red_amount), getRedAmount());
            json.put(getKey(R.string.pref_red_intensity), getRedIntensity());
            json.put(getKey(R.string.pref_red_wavelength), getRedWavelength());
            json.put(getKey(R.string.pref_green_amount), getGreenAmount());
            json.put(getKey(R.string.pref_green_intensity), getGreenIntensity());
            json.put(getKey(R.string.pref_green_wavelength), getGreenWavelength());
            json.put(getKey(R.string.pref_blue_amount), getBlueAmount());
            json.put(getKey(R.string.pref_blue_intensity), getBlueIntensity());
            json.put(getKey(R.string.pref_blue_wavelength), getBlueWavelength());
            json.put(getKey(R.string.pref_speed1), getXPulseSpeed());
            json.put(getKey(R.string.pref_speed2), getXMoveSpeed());
            json.put(getKey(R.string.pref_speed3), getYPulseSpeed());
            json.put(getKey(R.string.pref_speed4), getYMoveSpeed());
        }
        catch (JSONException ex) {
            throw new RuntimeException("org.clangen.gfx.plasma.Settings.toString() failed");
        }

        return json.toString();
    }

    public boolean fromString(String string) {
        try {
            // reading an invalid key will throw an exception
            JSONObject json = new JSONObject(string);
            int size = json.getInt(getKey(R.string.pref_size));
            int redAmount = json.getInt(getKey(R.string.pref_red_amount));
            int redIntensity = json.getInt(getKey(R.string.pref_red_intensity));
            int redWavelength = json.getInt(getKey(R.string.pref_red_wavelength));
            int greenAmount = json.getInt(getKey(R.string.pref_green_amount));
            int greenIntensity = json.getInt(getKey(R.string.pref_green_intensity));
            int greenWavelength = json.getInt(getKey(R.string.pref_green_wavelength));
            int blueAmount = json.getInt(getKey(R.string.pref_blue_amount));
            int blueIntensity = json.getInt(getKey(R.string.pref_blue_intensity));
            int blueWavelength = json.getInt(getKey(R.string.pref_blue_wavelength));
            int speed1 = json.getInt(getKey(R.string.pref_speed1));
            int speed2 = json.getInt(getKey(R.string.pref_speed2));
            int speed3 = json.getInt(getKey(R.string.pref_speed3));
            int speed4 = json.getInt(getKey(R.string.pref_speed4));

            // only set once we're sure we have the full set of values!
            try {
                beginTransaction();

                setSize(size);
                setRedAmount(redAmount);
                setRedIntensity(redIntensity);
                setRedWavelength(redWavelength);
                setGreenAmount(greenAmount);
                setGreenIntensity(greenIntensity);
                setGreenWavelength(greenWavelength);
                setBlueAmount(blueAmount);
                setBlueIntensity(blueIntensity);
                setBlueWavelength(blueWavelength);
                setXPulseSpeed(speed1);
                setXMoveSpeed(speed2);
                setYPulseSpeed(speed3);
                setYMoveSpeed(speed4);
            }
            finally {
                endTransaction();
            }
        }
        catch (JSONException ex) {
        }

        return false;
    }

    private void broadcastChanged() {
        mContext.sendBroadcast(new Intent(ACTION_EFFECT_CHANGED));
    }
}
