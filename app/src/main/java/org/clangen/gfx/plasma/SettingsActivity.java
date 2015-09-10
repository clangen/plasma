package org.clangen.gfx.plasma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.ViewFlipper;

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";

    public static final String ACTION_SETTINGS_STARTED = "org.clangen.gfx.plasma.ACTION_SETTINGS_STARTED";
    public static final String ACTION_SETTINGS_FINISHED = "org.clangen.gfx.plasma.ACTION_SETTINGS_FINISHED";

    private static final int VIEW_STATE_EDITOR = 0;
    private static final int VIEW_STATE_LIBRARY = 1;

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 9;
    private static final int SIZE_STEP_COUNT = MAX_SIZE - MIN_SIZE;
    private static final int MIN_MOVE = 1;
    private static final int MIN_SPEED = -10;
    private static final int SPEED_DISPLAY_OFFSET = (MIN_SPEED * -1);
    private static final int TOTAL_MOVEMENT_STEPS = (Math.abs(MIN_SPEED) * 2);
    private static final int TOTAL_SHAPE_STEPS = 10;
    private static final int MAX_COLOR = 255;
    private static final int MAX_SCALAR = 70;
    private static final int MAX_WAVELENGTH = 140;

    private static boolean sIsActive = false;

    private Views mViews = new Views();
    private Effect mEffect;
    private ProfileLibrary mProfileLibrary;
    private SimpleCursorAdapter mListAdapter;
    private SharedPreferences mPrefs;
    private boolean mPaused = true;
    private Plasma mPlasma;
    private int mViewState = VIEW_STATE_EDITOR;

    private static class Views {
        SurfaceView mSurfaceView;
        SeekBar mSizeSeekBar;
        SeekBar mXMoveModifier1SeekBar;
        SeekBar mXMoveModifier2SeekBar;
        SeekBar mYMoveModifier1SeekBar;
        SeekBar mYMoveModifier2SeekBar;
        SeekBar mXShapeModifier1SeekBar;
        SeekBar mXShapeModifier2SeekBar;
        SeekBar mYShapeModifier1SeekBar;
        SeekBar mYShapeModifier2SeekBar;
        SeekBar mRedBrightnessSeekBar;
        SeekBar mRedContrastSeekBar;
        SeekBar mRedFrequencySeekBar;
        SeekBar mGreenBrightnessSeekBar;
        SeekBar mGreenContrastSeekBar;
        SeekBar mGreenFrequencySeekBar;
        SeekBar mBlueBrightnessSeekBar;
        SeekBar mBlueContrastSeekBar;
        SeekBar mBlueFrequencySeekBar;
        ListView mProfilesListView;
        Button mEditorButton;
        Button mLibraryButton;
        ViewFlipper mFlipper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEffect = Effect.getInstance(this);
        mProfileLibrary = ProfileLibrary.getInstance(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.settings);

        mPlasma = Plasma.getInstance(this);

        mViews.mSurfaceView = (SurfaceView) findViewById(R.id.SurfaceView);
        mViews.mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);

        mViews.mSizeSeekBar = configureSeekBar(R.id.SizeSeekBar, SIZE_STEP_COUNT);

        mViews.mXMoveModifier2SeekBar = configureSeekBar(R.id.XMove1SeekBar, TOTAL_MOVEMENT_STEPS);
        mViews.mXMoveModifier1SeekBar = configureSeekBar(R.id.XMove2SeekBar, TOTAL_MOVEMENT_STEPS);
        mViews.mYMoveModifier2SeekBar = configureSeekBar(R.id.YMove1SeekBar, TOTAL_MOVEMENT_STEPS);
        mViews.mYMoveModifier1SeekBar = configureSeekBar(R.id.YMove2SeekBar, TOTAL_MOVEMENT_STEPS);

        mViews.mXShapeModifier2SeekBar = configureSeekBar(R.id.XShape1SeekBar, TOTAL_SHAPE_STEPS);
        mViews.mXShapeModifier1SeekBar = configureSeekBar(R.id.XShape2SeekBar, TOTAL_SHAPE_STEPS);
        mViews.mYShapeModifier2SeekBar = configureSeekBar(R.id.YShape1SeekBar, TOTAL_SHAPE_STEPS);
        mViews.mYShapeModifier1SeekBar = configureSeekBar(R.id.YShape2SeekBar, TOTAL_SHAPE_STEPS);

        /* NOTE RED AND BLUE ARE SWAPPED DUE TO AN OLD STUPID BUG */
        mViews.mRedBrightnessSeekBar = configureSeekBar(R.id.BlueAmountSeekBar, MAX_COLOR);
        mViews.mRedContrastSeekBar = configureSeekBar(R.id.BlueIntensitySeekBar, MAX_SCALAR);
        mViews.mRedFrequencySeekBar = configureSeekBar(R.id.BlueWavelengthSeekBar, MAX_WAVELENGTH);

        mViews.mGreenBrightnessSeekBar = configureSeekBar(R.id.GreenAmountSeekBar, MAX_COLOR);
        mViews.mGreenContrastSeekBar = configureSeekBar(R.id.GreenIntensitySeekBar, MAX_SCALAR);
        mViews.mGreenFrequencySeekBar = configureSeekBar(R.id.GreenWavelengthSeekBar, MAX_WAVELENGTH);

        /* NOTE BLUE AND RED ARE SWAPPED DUE TO AN OLD STUPID BUG */
        mViews.mBlueBrightnessSeekBar = configureSeekBar(R.id.RedAmountSeekBar, MAX_COLOR);
        mViews.mBlueContrastSeekBar = configureSeekBar(R.id.RedIntensitySeekBar, MAX_SCALAR);
        mViews.mBlueFrequencySeekBar = configureSeekBar(R.id.RedWavelengthSeekBar, MAX_WAVELENGTH);

        mViews.mFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
        mViews.mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        mViews.mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        mViews.mFlipper.setAnimateFirstView(true);

        mViews.mProfilesListView = (ListView) findViewById(R.id.ProfilesListView);
        mViews.mProfilesListView.setOnItemClickListener(mOnProfileRowClickListener);
        registerForContextMenu(mViews.mProfilesListView);

        findViewById(R.id.ResetButton).setOnClickListener(mResetClickListener);
        findViewById(R.id.OKButton).setOnClickListener(mDoneClickListener);
        findViewById(R.id.SaveButton).setOnClickListener(mSaveButton1ClickListener);

        mViews.mLibraryButton = (Button) findViewById(R.id.LibraryButton);
        mViews.mLibraryButton.setOnClickListener(mLibraryClickListener);

        mViews.mEditorButton = (Button) findViewById(R.id.EditorButton);
        mViews.mEditorButton.setOnClickListener(mEditorClickListener);

        reloadSettings();
        reloadProfiles();

        Log.i("Plasma", "Loaded effect: " + mEffect.toString());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view == mViews.mProfilesListView) {
            getMenuInflater().inflate(R.menu.profilescontextmenu, menu);
            menu.setHeaderTitle(R.string.context_menu_title);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getMenuInfo() == null) {
            return false;
        }

        AdapterContextMenuInfo itemInfo;
        itemInfo = (AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
        case R.id.ProfileListMenuRename:
            showEditProfileNameDialog(itemInfo.id);

            return true;

        case R.id.ProfileListMenuReplace:
            showConfirmReplaceProfileDialog(itemInfo.id);
            return true;

        case R.id.ProfileListMenuDelete:
            showConfirmProfileDeleteDialog(itemInfo.id);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public static boolean isActive() {
        return sIsActive;
    }

    private SeekBar configureSeekBar(int id, int max) {
        SeekBar seekbar = (SeekBar) findViewById(id);
        seekbar.setMax(max);
        seekbar.setOnSeekBarChangeListener(mOnSeekBarChangedListener);
        return seekbar;
    }

    protected void onResume() {
        super.onResume();
        sIsActive = true;
        mPaused = false;
        sendBroadcast(new Intent(ACTION_SETTINGS_STARTED));
        addBuiltInProfiles();
    }

    protected void onPause() {
        super.onPause();
        sIsActive = false;
        mPaused = true;
        sendBroadcast(new Intent(ACTION_SETTINGS_FINISHED));
    }

    private boolean builtInProfilesAdded() {
        String key = getString(R.string.pref_built_in_profiles_added);
        return (mPrefs.getBoolean(key, false));
    }

    private boolean cottonCandyProfileAdded() {
        String key = getString(R.string.pref_cotton_candy_added);
        return (mPrefs.getBoolean(key, false));
    }

    private void addBuiltInProfiles() {
        if ((!builtInProfilesAdded()) || (!cottonCandyProfileAdded())) {
            new ProfileLoader().execute(new Void[] { });
        }
    }

    private void reloadSettings() {
        mViews.mSizeSeekBar.setProgress(MIN_SIZE + (SIZE_STEP_COUNT - Math.max(0, mEffect.getSize())));

        mViews.mXMoveModifier1SeekBar.setProgress(TOTAL_MOVEMENT_STEPS - (mEffect.getXMoveModifier2() + SPEED_DISPLAY_OFFSET));
        mViews.mYMoveModifier1SeekBar.setProgress(mEffect.getYMoveModifier2() + SPEED_DISPLAY_OFFSET);
        mViews.mXMoveModifier2SeekBar.setProgress(mEffect.getXMoveModifier1() + SPEED_DISPLAY_OFFSET);
        mViews.mYMoveModifier2SeekBar.setProgress(mEffect.getYMoveModifier1() + SPEED_DISPLAY_OFFSET);

        mViews.mXShapeModifier1SeekBar.setProgress(mEffect.getXShapeModifier1());
        mViews.mXShapeModifier2SeekBar.setProgress(mEffect.getXShapeModifier2());
        mViews.mYShapeModifier1SeekBar.setProgress(mEffect.getYShapeModifier1());
        mViews.mYShapeModifier2SeekBar.setProgress(mEffect.getYShapeModifier2());

        mViews.mRedBrightnessSeekBar.setProgress(mEffect.getRedBrightness());
        mViews.mRedContrastSeekBar.setProgress(mEffect.getRedContrast());
        mViews.mRedFrequencySeekBar.setProgress(mEffect.getRedFrequency());

        mViews.mGreenBrightnessSeekBar.setProgress(mEffect.getGreenBrightness());
        mViews.mGreenContrastSeekBar.setProgress(mEffect.getGreenContrast());
        mViews.mGreenFrequencySeekBar.setProgress(mEffect.getGreenFrequency());

        mViews.mBlueBrightnessSeekBar.setProgress(mEffect.getBlueBrightness());
        mViews.mBlueContrastSeekBar.setProgress(mEffect.getBlueContrast());
        mViews.mBlueFrequencySeekBar.setProgress(mEffect.getBlueFrequency());
    }

    private void reloadProfiles() {
        Cursor cursor = mProfileLibrary.getProfileNames();
        startManagingCursor(cursor);

        mListAdapter = new SimpleCursorAdapter(
            this,
            R.layout.profilerow,
            cursor,
            new String[] { ProfileLibrary.NAME_COLUMN },
            new int[] { R.id.ProfileRowName });

        mViews.mProfilesListView.setAdapter(mListAdapter);
    }

    private void showInvalidNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.invalid_name_title);
        builder.setMessage(R.string.invalid_name_message);
        builder.setPositiveButton(R.string.button_close_app, null);
        builder.show();
    }

    private void showConfirmResetEffectLibraryDialog() {
        if (mViewState == VIEW_STATE_EDITOR) {
            flipToLibrary();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_reset_profiles_title);
        builder.setMessage(R.string.confirm_reset_profiles_message);
        builder.setPositiveButton(R.string.button_yes, mOnResetEffectLibrary);
        builder.setNegativeButton(R.string.button_no, null);
        builder.show();
    }

    private void showConfirmDialog(String title, String message, DialogInterface.OnClickListener onYes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_yes, onYes);
        builder.setNegativeButton(R.string.button_no, null);
        builder.show();
    }

    private void showProfileNameEntryDialog(
        String text,
        DialogInterface.OnClickListener onSave,
        DialogInterface.OnClickListener onCancel,
        DialogInterface.OnCancelListener onDialogCancel)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.name_effect_title);
        builder.setView(View.inflate(this, R.layout.profilenamedialog, null));
        builder.setPositiveButton(R.string.button_save, onSave);
        builder.setNegativeButton(R.string.button_cancel, onCancel);
        builder.setOnCancelListener(onDialogCancel);

        Dialog dlg = builder.show();

        EditText editText = (EditText) dlg.findViewById(R.id.ProfileNameEditText);
        editText.setText(text == null ? "" : text);
    }

    private void showConfirmResetDialog() {
        showConfirmDialog(
            getString(R.string.confirm_reset_title),
            getString(R.string.confirm_reset_message),
            mConfirmResetDefaultsListener);
    }

    private void showConfirmProfileDeleteDialog(long id) {
        String message = getString(
            R.string.confirm_delete_message,
            mProfileLibrary.getNameById(id));

        showConfirmDialog(
            getString(R.string.confirm_delete_title),
            message,
            new ConfirmDeleteProfileListener(id));
    }

    private void showConfirmReplaceProfileDialog(long id) {
        String message = getString(
            R.string.confirm_replace_message,
            mProfileLibrary.getNameById(id));

        showConfirmDialog(
            getString(R.string.confirm_replace_title),
            message,
            new ConfirmReplaceProfileListener(id));
    }

    private void showNewProfileDialog() {
        showProfileNameEntryDialog(
            "",
            mSaveProfileDialogClickListener,
            mSaveProfileDialogCancelClickListener,
            mSaveProfileDialogCancelListener);
    }

    private void showEditProfileNameDialog(long id) {
        showProfileNameEntryDialog(
            mProfileLibrary.getNameById(id),
            new ConfirmRenameProfileListener(id),
            null,
            null);
    }

    private String getProfileNameFromDialog(DialogInterface dialogInterface) {
        Dialog dialog = (Dialog) dialogInterface;
        EditText edit = (EditText) dialog.findViewById(R.id.ProfileNameEditText);

        String name = edit.getText().toString().trim();
        if (name.length() == 0) {
            showInvalidNameDialog();
            return null;
        }

        return name;
    }

    private void flipToLibrary() {
        mViews.mFlipper.showPrevious();
        mViewState = VIEW_STATE_LIBRARY;
    }

    private void flipToEditor() {
        mViews.mFlipper.showNext();
        mViewState = VIEW_STATE_EDITOR;
    }

    private boolean isLowPriorityEnabled() {
        return mPrefs.getBoolean(getString(R.string.pref_low_priority), true);
    }

    private OnItemClickListener mOnProfileRowClickListener =
        new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                String settings = mProfileLibrary.getSettingsById(id);
                mEffect.fromString(settings);
                reloadSettings();
            }
        };

    private DialogInterface.OnClickListener mConfirmResetDefaultsListener =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mEffect.resetEffectToDefault();
                reloadSettings();
            }
        };

    private class ConfirmRenameProfileListener implements DialogInterface.OnClickListener {
        private long mId;

        public ConfirmRenameProfileListener(long id) {
            mId = id;
        }

        public void onClick(DialogInterface dialog, int which) {
            String name = getProfileNameFromDialog(dialog);
            if (name != null) {
                mProfileLibrary.renameProfile(mId, name);
                mListAdapter.getCursor().requery();
            }
        }
    }

    private class ConfirmDeleteProfileListener implements DialogInterface.OnClickListener {
        long mId;

        public ConfirmDeleteProfileListener(long id) {
            mId = id;
        }

        public void onClick(DialogInterface dialog, int which) {
            mProfileLibrary.deleteProfile(mId);
            mListAdapter.getCursor().requery();
        }
    }

    private class ConfirmReplaceProfileListener implements DialogInterface.OnClickListener {
        long mId;

        public ConfirmReplaceProfileListener(long id) {
            mId = id;
        }

        public void onClick(DialogInterface dialog, int which) {
            mProfileLibrary.updateProfile(mId, mEffect.toString());
            mListAdapter.getCursor().requery();
        }
    }

    private void resetBuiltInProfiles() {
        mProfileLibrary.deleteAllProfiles();

        SharedPreferences.Editor editor = mPrefs.edit();

        String key = getString(R.string.pref_built_in_profiles_added);
        editor.putBoolean(key, false);

        key = getString(R.string.pref_cotton_candy_added);
        editor.putBoolean(key, false);

        editor.apply();

        reloadProfiles();
    }

    private DialogInterface.OnClickListener mOnResetEffectLibrary =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                resetBuiltInProfiles();
                addBuiltInProfiles();
            }
        };

    private DialogInterface.OnClickListener mSaveProfileDialogClickListener =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = getProfileNameFromDialog(dialog);
                if (name != null) {
                    mProfileLibrary.addProfile(name, mEffect.toString());
                    mListAdapter.getCursor().requery();
                }
            }
        };

    private DialogInterface.OnClickListener mSaveProfileDialogCancelClickListener =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                flipToEditor();
            }
        };

    private DialogInterface.OnCancelListener mSaveProfileDialogCancelListener =
        new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                flipToEditor();
            }
        };

    private OnClickListener mSaveButton1ClickListener = new OnClickListener() {
        public void onClick(View view) {
            flipToLibrary();
            showNewProfileDialog();
        }
    };

    private OnClickListener mResetClickListener = new OnClickListener() {
        public void onClick(View view) {
            showConfirmResetDialog();
        }
    };

    private OnClickListener mDoneClickListener = new OnClickListener() {
        public void onClick(View view) {
            finish();
        }
    };

    private OnClickListener mLibraryClickListener = new OnClickListener() {
        public void onClick(View view) {
            flipToLibrary();
        }
    };

    private OnClickListener mEditorClickListener = new OnClickListener() {
        public void onClick(View view) {
            flipToEditor();
        }
    };

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

    private OnSeekBarChangeListener mOnSeekBarChangedListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (seekBar == mViews.mSizeSeekBar) {
                mEffect.setSize(MIN_SIZE + (SIZE_STEP_COUNT - progress));
            }
            else if (seekBar == mViews.mXMoveModifier1SeekBar) {
                mEffect.setXMoveModifier2(MIN_MOVE - (progress - SPEED_DISPLAY_OFFSET));
            }
            else if (seekBar == mViews.mYMoveModifier1SeekBar) {
                mEffect.setYMoveModifier2(MIN_MOVE - (progress - SPEED_DISPLAY_OFFSET));
            }
            else if (seekBar == mViews.mXMoveModifier2SeekBar) {
                mEffect.setXMoveModifier1(progress - SPEED_DISPLAY_OFFSET);
            }
            else if (seekBar == mViews.mYMoveModifier2SeekBar) {
                mEffect.setYMoveModifier1(progress - SPEED_DISPLAY_OFFSET);
            }
            else if (seekBar == mViews.mXShapeModifier1SeekBar) {
                mEffect.setXShapeModifier1(progress);
            }
            else if (seekBar == mViews.mXShapeModifier2SeekBar) {
                mEffect.setXShapeModifier2(progress);
            }
            else if (seekBar == mViews.mYShapeModifier1SeekBar) {
                mEffect.setYShapeModifier1(progress);
            }
            else if (seekBar == mViews.mYShapeModifier2SeekBar) {
                mEffect.setYShapeModifier2(progress);
            }
            else if (seekBar == mViews.mRedBrightnessSeekBar) {
                mEffect.setRedBrightness(progress);
            }
            else if (seekBar == mViews.mRedContrastSeekBar) {
                mEffect.setRedContrast(progress);
            }
            else if (seekBar == mViews.mRedFrequencySeekBar) {
                mEffect.setRedFrequency(progress);
            }
            else if (seekBar == mViews.mGreenBrightnessSeekBar) {
                mEffect.setGreenBrightness(progress);
            }
            else if (seekBar == mViews.mGreenContrastSeekBar) {
                mEffect.setGreenContrast(progress);
            }
            else if (seekBar == mViews.mGreenFrequencySeekBar) {
                mEffect.setGreenFrequency(progress);
            }
            else if (seekBar == mViews.mBlueBrightnessSeekBar) {
                mEffect.setBlueBrightness(progress);
            }
            else if (seekBar == mViews.mBlueContrastSeekBar) {
                mEffect.setBlueContrast(progress);
            }
            else if (seekBar == mViews.mBlueFrequencySeekBar) {
                mEffect.setBlueFrequency(progress);
            }
        }
    };

    private class ProfileLoader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mPaused && mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            reloadProfiles();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!mPaused) {
                mProgressDialog =
                    ProgressDialog.show(
                        SettingsActivity.this,
                        getString(R.string.library_init_title),
                        getString(R.string.library_init_message));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (ProfileLoader.class) {
                if (!builtInProfilesAdded()) {
                    Log.i(TAG, "Profiles don't exist, creating...");

                    if (mProfileLibrary.addBuiltInProfiles()) {
                        setPreferenceBoolean(
                            R.string.pref_built_in_profiles_added);

                        Log.i(TAG, "Profiles don't exist, created!");
                    }
                }
            }

            return null;
        }

        protected void setPreferenceBoolean(int id) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(getString(id), true);
            editor.apply();
        }
    }
}
