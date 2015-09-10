package org.clangen.gfx.plasma;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Effect {
    /* the default preset is watermelon 1 */
    private final static int DEFAULT_SIZE = 1;
    private final static int DEFAULT_RED_BRIGHTNESS = 42;
    private final static int DEFAULT_RED_CONTRAST = 11;
    private final static int DEFAULT_RED_FREQUENCY = 0;
    private final static int DEFAULT_GREEN_BRIGHTNESS = 27;
    private final static int DEFAULT_GREEN_CONTRAST = 36;
    private final static int DEFAULT_GREEN_FREQUENCY = 16;
    private final static int DEFAULT_BLUE_BRIGHTNESS = 90;
    private final static int DEFAULT_BLUE_CONTRAST = 67;
    private final static int DEFAULT_BLUE_FREQUENCY = 140;
    private final static int DEFAULT_X_MOVE_MODIFIER_1 = -5;
    private final static int DEFAULT_X_MOVE_MODIFIER_2 = 7;
    private final static int DEFAULT_Y_MOVE_MODIFIER_1 = 0;
    private final static int DEFAULT_Y_MOVE_MODIFIER_2 = 0;
    private final static int DEFAULT_X_SHAPE_MODIFIER_1 = 1;
    private final static int DEFAULT_X_SHAPE_MODIFIER_2 = 2;
    private final static int DEFAULT_Y_SHAPE_MODIFIER_1 = 1;
    private final static int DEFAULT_Y_SHAPE_MODIFIER_2 = 2;

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
        return Math.max(1, getInteger(R.string.pref_size, DEFAULT_SIZE));
    }

    public void setSize(int size) {
        setInteger(R.string.pref_size, size);
    }

    public int getXMoveModifier1() {
        return getInteger(R.string.pref_x_move_modifier_1, DEFAULT_X_MOVE_MODIFIER_1);
    }

    public void setXMoveModifier1(int speed) {
        setInteger(R.string.pref_x_move_modifier_1, speed);
    }

    public int getXMoveModifier2() {
        return getInteger(R.string.pref_x_move_modifier_2, DEFAULT_X_MOVE_MODIFIER_2);
    }

    public void setXMoveModifier2(int speed) {
        setInteger(R.string.pref_x_move_modifier_2, speed);
    }

    public int getYMoveModifier1() {
        return getInteger(R.string.pref_y_move_modifier_1, DEFAULT_Y_MOVE_MODIFIER_1);
    }

    public void setYMoveModifier1(int speed) {
        setInteger(R.string.pref_y_move_modifier_1, speed);
    }

    public int getYMoveModifier2() {
        return getInteger(R.string.pref_y_move_modifier_2, DEFAULT_Y_MOVE_MODIFIER_2);
    }

    public void setYMoveModifier2(int speed) {
        setInteger(R.string.pref_y_move_modifier_2, speed);
    }

    public int getXShapeModifier1() {
        return getInteger(R.string.pref_x_shape_modifier_1, DEFAULT_X_SHAPE_MODIFIER_1);
    }

    public void setXShapeModifier1(int value) {
        setInteger(R.string.pref_x_shape_modifier_1, value);
    }

    public int getXShapeModifier2() {
        return getInteger(R.string.pref_x_shape_modifier_2, DEFAULT_X_SHAPE_MODIFIER_2);
    }

    public void setXShapeModifier2(int value) {
        setInteger(R.string.pref_x_shape_modifier_2, value);
    }

    public int getYShapeModifier1() {
        return getInteger(R.string.pref_y_shape_modifier_1, DEFAULT_Y_SHAPE_MODIFIER_1);
    }

    public void setYShapeModifier1(int value) {
        setInteger(R.string.pref_y_shape_modifier_1, value);
    }

    public int getYShapeModifier2() {
        return getInteger(R.string.pref_y_shape_modifier_2, DEFAULT_Y_SHAPE_MODIFIER_2);
    }

    public void setYShapeModifier2(int value) {
        setInteger(R.string.pref_y_shape_modifier_2, value);
    }

    public int getRedBrightness() {
        return getInteger(R.string.pref_red_brightness, DEFAULT_RED_BRIGHTNESS);
    }

    public void setRedBrightness(int value) {
        setInteger(R.string.pref_red_brightness, value);
    }

    public int getRedContrast() {
        return getInteger(R.string.pref_red_contrast, DEFAULT_RED_CONTRAST);
    }

    public void setRedContrast(int value) {
        setInteger(R.string.pref_red_contrast, value);
    }

    public int getRedFrequency() {
        return getInteger(R.string.pref_red_frequency, DEFAULT_RED_FREQUENCY);
    }

    public void setRedFrequency(int value) {
        setInteger(R.string.pref_red_frequency, value);
    }

    public int getGreenBrightness() {
        return getInteger(R.string.pref_green_brightness, DEFAULT_GREEN_BRIGHTNESS);
    }

    public void setGreenBrightness(int value) {
        setInteger(R.string.pref_green_brightness, value);
    }

    public int getGreenContrast() {
        return getInteger(R.string.pref_green_contrast, DEFAULT_GREEN_CONTRAST);
    }

    public void setGreenContrast(int value) {
        setInteger(R.string.pref_green_contrast, value);
    }

    public int getGreenFrequency() {
        return getInteger(R.string.pref_green_frequency, DEFAULT_GREEN_FREQUENCY);
    }

    public void setGreenFrequency(int value) {
        setInteger(R.string.pref_green_frequency, value);
    }

    public int getBlueBrightness() {
        return getInteger(R.string.pref_blue_brightness, DEFAULT_BLUE_BRIGHTNESS);
    }

    public void setBlueBrightness(int value) {
        setInteger(R.string.pref_blue_brightness, value);
    }

    public int getBlueContrast() {
        return getInteger(R.string.pref_blue_contrast, DEFAULT_BLUE_CONTRAST);
    }

    public void setBlueContrast(int value) {
        setInteger(R.string.pref_blue_contrast, value);
    }

    public int getBlueFrequency() {
        return getInteger(R.string.pref_blue_frequency, DEFAULT_BLUE_FREQUENCY);
    }

    public void setBlueFrequency(int value) {
        setInteger(R.string.pref_blue_frequency, value);
    }

    public void resetEffectToDefault() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(getKey(R.string.pref_size));
        editor.remove(getKey(R.string.pref_red_brightness));
        editor.remove(getKey(R.string.pref_red_contrast));
        editor.remove(getKey(R.string.pref_red_frequency));
        editor.remove(getKey(R.string.pref_green_brightness));
        editor.remove(getKey(R.string.pref_green_contrast));
        editor.remove(getKey(R.string.pref_green_frequency));
        editor.remove(getKey(R.string.pref_blue_brightness));
        editor.remove(getKey(R.string.pref_blue_contrast));
        editor.remove(getKey(R.string.pref_blue_frequency));
        editor.remove(getKey(R.string.pref_x_move_modifier_1));
        editor.remove(getKey(R.string.pref_x_move_modifier_2));
        editor.remove(getKey(R.string.pref_y_move_modifier_1));
        editor.remove(getKey(R.string.pref_y_move_modifier_2));
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
            json.put(getKey(R.string.pref_red_brightness), getRedBrightness());
            json.put(getKey(R.string.pref_red_contrast), getRedContrast());
            json.put(getKey(R.string.pref_red_frequency), getRedFrequency());
            json.put(getKey(R.string.pref_green_brightness), getGreenBrightness());
            json.put(getKey(R.string.pref_green_contrast), getGreenContrast());
            json.put(getKey(R.string.pref_green_frequency), getGreenFrequency());
            json.put(getKey(R.string.pref_blue_brightness), getBlueBrightness());
            json.put(getKey(R.string.pref_blue_contrast), getBlueContrast());
            json.put(getKey(R.string.pref_blue_frequency), getBlueFrequency());
            json.put(getKey(R.string.pref_x_move_modifier_1), getXMoveModifier1());
            json.put(getKey(R.string.pref_x_move_modifier_2), getXMoveModifier2());
            json.put(getKey(R.string.pref_y_move_modifier_1), getYMoveModifier1());
            json.put(getKey(R.string.pref_y_move_modifier_2), getYMoveModifier2());
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
            int redBrightness = json.getInt(getKey(R.string.pref_red_brightness));
            int redContrast = json.getInt(getKey(R.string.pref_red_contrast));
            int redFrequency = json.getInt(getKey(R.string.pref_red_frequency));
            int greenBrightness = json.getInt(getKey(R.string.pref_green_brightness));
            int greenContrast = json.getInt(getKey(R.string.pref_green_contrast));
            int greenFrequency = json.getInt(getKey(R.string.pref_green_frequency));
            int blueBrightness = json.getInt(getKey(R.string.pref_blue_brightness));
            int blueContrast = json.getInt(getKey(R.string.pref_blue_contrast));
            int blueFrequency = json.getInt(getKey(R.string.pref_blue_frequency));
            int xMoveModifier1 = json.getInt(getKey(R.string.pref_x_move_modifier_1));
            int xMoveModifier2 = json.getInt(getKey(R.string.pref_x_move_modifier_2));
            int yMoveModifier1 = json.getInt(getKey(R.string.pref_y_move_modifier_1));
            int yMoveModifier2 = json.getInt(getKey(R.string.pref_y_move_modifier_2));
            int xShapeModifier1 = json.optInt(getKey(R.string.pref_x_shape_modifier_1), DEFAULT_X_SHAPE_MODIFIER_1);
            int xShapeModifier2 = json.optInt(getKey(R.string.pref_x_shape_modifier_2), DEFAULT_X_SHAPE_MODIFIER_2);
            int yShapeModifier1 = json.optInt(getKey(R.string.pref_y_shape_modifier_1), DEFAULT_Y_SHAPE_MODIFIER_1);
            int yShapeModifier2 = json.optInt(getKey(R.string.pref_y_shape_modifier_2), DEFAULT_Y_SHAPE_MODIFIER_2);

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
        Plasma.onEffectChanged();
    }
}
