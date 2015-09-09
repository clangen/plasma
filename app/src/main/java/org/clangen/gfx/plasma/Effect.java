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
    private final static int X_MOVE_MODIFIER_1 = 3;
    private final static int X_MOVE_MODIFIER_2 = 1;
    private final static int Y_MOVE_MODIFIER_1 = 8;
    private final static int Y_MOVE_MODIFIER_2 = 1;
    private final static int X_SHAPE_MODIFIER_1 = 1;
    private final static int X_SHAPE_MODIFIER_2 = 2;
    private final static int Y_SHAPE_MODIFIER_1 = 1;
    private final static int Y_SHAPE_MODIFIER_2 = 2;

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

    public int getXMoveModifier1() {
        return getInteger(R.string.pref_speed1, X_MOVE_MODIFIER_1);
    }

    public void setXMoveModifier1(int speed) {
        setInteger(R.string.pref_speed1, speed);
    }

    public int getXMoveModifier2() {
        return getInteger(R.string.pref_speed2, X_MOVE_MODIFIER_2);
    }

    public void setXMoveModifier2(int speed) {
        setInteger(R.string.pref_speed2, speed);
    }

    public int getYMoveModifier1() {
        return getInteger(R.string.pref_speed3, Y_MOVE_MODIFIER_1);
    }

    public void setYMoveModifier1(int speed) {
        setInteger(R.string.pref_speed3, speed);
    }

    public int getYMoveModifier2() {
        return getInteger(R.string.pref_speed4, Y_MOVE_MODIFIER_2);
    }

    public void setYMoveModifier2(int speed) {
        setInteger(R.string.pref_speed4, speed);
    }

    public int getXShapeModifier1() {
        return getInteger(R.string.pref_x_shape_modifier_1, X_SHAPE_MODIFIER_1);
    }

    public void setXShapeModifier1(int value) {
        setInteger(R.string.pref_x_shape_modifier_1, value);
    }

    public int getXShapeModifier2() {
        return getInteger(R.string.pref_x_shape_modifier_2, X_SHAPE_MODIFIER_2);
    }

    public void setXShapeModifier2(int value) {
        setInteger(R.string.pref_x_shape_modifier_2, value);
    }

    public int getYShapeModifier1() {
        return getInteger(R.string.pref_y_shape_modifier_1, Y_SHAPE_MODIFIER_1);
    }

    public void setYShapeModifier1(int value) {
        setInteger(R.string.pref_y_shape_modifier_1, value);
    }

    public int getYShapeModifier2() {
        return getInteger(R.string.pref_y_shape_modifier_2, Y_SHAPE_MODIFIER_2);
    }

    public void setYShapeModifier2(int value) {
        setInteger(R.string.pref_y_shape_modifier_2, value);
    }

    public int getRedBrightness() {
        return getInteger(R.string.pref_red_amount, R_AMOUNT);
    }

    public void setRedBrightness(int value) {
        setInteger(R.string.pref_red_amount, value);
    }

    public int getRedContrast() {
        return getInteger(R.string.pref_red_intensity, R_INTENSITY);
    }

    public void setRedContrast(int value) {
        setInteger(R.string.pref_red_intensity, value);
    }

    public int getRedFrequency() {
        return getInteger(R.string.pref_red_wavelength, R_WAVELENGTH);
    }

    public void setRedFrequency(int value) {
        setInteger(R.string.pref_red_wavelength, value);
    }

    public int getGreenBrightness() {
        return getInteger(R.string.pref_green_amount, G_AMOUNT);
    }

    public void setGreenBrightness(int value) {
        setInteger(R.string.pref_green_amount, value);
    }

    public int getGreenContrast() {
        return getInteger(R.string.pref_green_intensity, G_INTENSITY);
    }

    public void setGreenContrast(int value) {
        setInteger(R.string.pref_green_intensity, value);
    }

    public int getGreenFrequency() {
        return getInteger(R.string.pref_green_wavelength, G_WAVELENGTH);
    }

    public void setGreenFrequency(int value) {
        setInteger(R.string.pref_green_wavelength, value);
    }

    public int getBlueBrightness() {
        return getInteger(R.string.pref_blue_amount, B_AMOUNT);
    }

    public void setBlueBrightness(int value) {
        setInteger(R.string.pref_blue_amount, value);
    }

    public int getBlueContrast() {
        return getInteger(R.string.pref_blue_intensity, B_INTENSITY);
    }

    public void setBlueContrast(int value) {
        setInteger(R.string.pref_blue_intensity, value);
    }

    public int getBlueFrequency() {
        return getInteger(R.string.pref_blue_wavelength, B_WAVELENGTH);
    }

    public void setBlueFrequency(int value) {
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
        editor.remove(getKey(R.string.pref_x_shape_modifier_1));
        editor.remove(getKey(R.string.pref_x_shape_modifier_2));
        editor.remove(getKey(R.string.pref_y_shape_modifier_1));
        editor.remove(getKey(R.string.pref_y_shape_modifier_2));
        editor.apply();

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
            if (!mTransaction) {
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
            json.put(getKey(R.string.pref_red_amount), getRedBrightness());
            json.put(getKey(R.string.pref_red_intensity), getRedContrast());
            json.put(getKey(R.string.pref_red_wavelength), getRedFrequency());
            json.put(getKey(R.string.pref_green_amount), getGreenBrightness());
            json.put(getKey(R.string.pref_green_intensity), getGreenContrast());
            json.put(getKey(R.string.pref_green_wavelength), getGreenFrequency());
            json.put(getKey(R.string.pref_blue_amount), getBlueBrightness());
            json.put(getKey(R.string.pref_blue_intensity), getBlueContrast());
            json.put(getKey(R.string.pref_blue_wavelength), getBlueFrequency());
            json.put(getKey(R.string.pref_speed1), getXMoveModifier1());
            json.put(getKey(R.string.pref_speed2), getXMoveModifier2());
            json.put(getKey(R.string.pref_speed3), getYMoveModifier1());
            json.put(getKey(R.string.pref_speed4), getYMoveModifier2());
            json.put(getKey(R.string.pref_x_shape_modifier_1), getXShapeModifier1());
            json.put(getKey(R.string.pref_x_shape_modifier_2), getXShapeModifier2());
            json.put(getKey(R.string.pref_y_shape_modifier_1), getYShapeModifier1());
            json.put(getKey(R.string.pref_y_shape_modifier_2), getYShapeModifier2());
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
            int redBrightness = json.getInt(getKey(R.string.pref_red_amount));
            int redContrast = json.getInt(getKey(R.string.pref_red_intensity));
            int redFrequency = json.getInt(getKey(R.string.pref_red_wavelength));
            int greenBrightness = json.getInt(getKey(R.string.pref_green_amount));
            int greenContrast = json.getInt(getKey(R.string.pref_green_intensity));
            int greenFrequency = json.getInt(getKey(R.string.pref_green_wavelength));
            int blueBrightness = json.getInt(getKey(R.string.pref_blue_amount));
            int blueContrast = json.getInt(getKey(R.string.pref_blue_intensity));
            int blueFrequency = json.getInt(getKey(R.string.pref_blue_wavelength));
            int xMoveModifier1 = json.getInt(getKey(R.string.pref_speed1));
            int xMoveModifier2 = json.getInt(getKey(R.string.pref_speed2));
            int yMoveModifier1 = json.getInt(getKey(R.string.pref_speed3));
            int yMoveModifier2 = json.getInt(getKey(R.string.pref_speed4));
            int xShapeModifier1 = json.optInt(getKey(R.string.pref_x_shape_modifier_1), X_SHAPE_MODIFIER_1);
            int xShapeModifier2 = json.optInt(getKey(R.string.pref_x_shape_modifier_2), X_SHAPE_MODIFIER_2);
            int yShapeModifier1 = json.optInt(getKey(R.string.pref_y_shape_modifier_1), Y_SHAPE_MODIFIER_1);
            int yShapeModifier2 = json.optInt(getKey(R.string.pref_y_shape_modifier_2), Y_SHAPE_MODIFIER_2);

            // only set once we're sure we have the full set of values!
            try {
                beginTransaction();

                setSize(size);
                setRedBrightness(redBrightness);
                setRedContrast(redContrast);
                setRedFrequency(redFrequency);
                setGreenBrightness(greenBrightness);
                setGreenContrast(greenContrast);
                setGreenFrequency(greenFrequency);
                setBlueBrightness(blueBrightness);
                setBlueContrast(blueContrast);
                setBlueFrequency(blueFrequency);
                setXMoveModifier1(xMoveModifier1);
                setXMoveModifier2(xMoveModifier2);
                setYMoveModifier1(yMoveModifier1);
                setYMoveModifier2(yMoveModifier2);
                setXShapeModifier1(xShapeModifier1);
                setXShapeModifier2(xShapeModifier2);
                setYShapeModifier1(yShapeModifier1);
                setYShapeModifier2(yShapeModifier2);
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
