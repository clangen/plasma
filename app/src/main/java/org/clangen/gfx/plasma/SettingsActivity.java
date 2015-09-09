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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";

    public static final String ACTION_SETTINGS_STARTED = "org.clangen.gfx.plasma.ACTION_SETTINGS_STARTED";
    public static final String ACTION_SETTINGS_FINISHED = "org.clangen.gfx.plasma.ACTION_SETTINGS_FINISHED";
    public static final String ACTION_SETTINGS_PRIORITY_CHANGED = "org.clangen.gfx.plasma.ACTION_PRIORITY_CHANGED";

    private static final int VIEW_STATE_BUILDER = 0;
    private static final int VIEW_STATE_LIBRARY = 1;

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 9;
    private static final int SIZE_STEP_COUNT = MAX_SIZE - MIN_SIZE;
    private static final int MIN_MOVE = 1;
    private static final int MIN_SPEED = -10;
    private static final int SPEED_DISPLAY_OFFSET = (MIN_SPEED * -1);
    private static final int TOTAL_SPEED_STEPS = (Math.abs(MIN_SPEED) * 2);
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
    private int mViewState = VIEW_STATE_BUILDER;

    private static class Views {
        SurfaceView mSurfaceView;
        SeekBar mSizeSeekBar;
        SeekBar mXMoveSpeedSeekBar;
        SeekBar mYMoveSpeedSeekBar;
        SeekBar mXPulseSpeedSeekBar;
        SeekBar mYPulseSpeedSeekBar;
        SeekBar mRedAmountSeekBar;
        SeekBar mRedIntensitySeekBar;
        SeekBar mRedWavelengthSeekBar;
        SeekBar mGreenAmountSeekBar;
        SeekBar mGreenIntensitySeekBar;
        SeekBar mGreenWavelengthSeekBar;
        SeekBar mBlueAmountSeekBar;
        SeekBar mBlueIntensitySeekBar;
        SeekBar mBlueWavelengthSeekBar;
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

        mViews.mXPulseSpeedSeekBar = configureSeekBar(R.id.Speed1SeekBar, TOTAL_SPEED_STEPS);
        mViews.mXMoveSpeedSeekBar = configureSeekBar(R.id.Speed2SeekBar, TOTAL_SPEED_STEPS);
        mViews.mYPulseSpeedSeekBar = configureSeekBar(R.id.Speed3SeekBar, TOTAL_SPEED_STEPS);
        mViews.mYMoveSpeedSeekBar = configureSeekBar(R.id.Speed4SeekBar, TOTAL_SPEED_STEPS);

        /* NOTE RED AND BLUE ARE SWAPPED DUE TO AN OLD STUPID BUG */
        mViews.mRedAmountSeekBar = configureSeekBar(R.id.BlueAmountSeekBar, MAX_COLOR);
        mViews.mRedIntensitySeekBar = configureSeekBar(R.id.BlueIntensitySeekBar, MAX_SCALAR);
        mViews.mRedWavelengthSeekBar = configureSeekBar(R.id.BlueWavelengthSeekBar, MAX_WAVELENGTH);

        mViews.mGreenAmountSeekBar = configureSeekBar(R.id.GreenAmountSeekBar, MAX_COLOR);
        mViews.mGreenIntensitySeekBar = configureSeekBar(R.id.GreenIntensitySeekBar, MAX_SCALAR);
        mViews.mGreenWavelengthSeekBar = configureSeekBar(R.id.GreenWavelengthSeekBar, MAX_WAVELENGTH);

        /* NOTE BLUE AND RED ARE SWAPPED DUE TO AN OLD STUPID BUG */
        mViews.mBlueAmountSeekBar = configureSeekBar(R.id.RedAmountSeekBar, MAX_COLOR);
        mViews.mBlueIntensitySeekBar = configureSeekBar(R.id.RedIntensitySeekBar, MAX_SCALAR);
        mViews.mBlueWavelengthSeekBar = configureSeekBar(R.id.RedWavelengthSeekBar, MAX_WAVELENGTH);

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
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.SettingsMenuPriorityToggle) {
            setLowPriorityEnabled( ! isLowPriorityEnabled());
        }
        else if (item.getItemId() == R.id.SettingsMenuResetProfiles) {
            showConfirmResetEffectLibraryDialog();
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem priority = menu.findItem(R.id.SettingsMenuPriorityToggle);

        priority.setTitle(isLowPriorityEnabled()
            ? R.string.settings_menu_raise_priority
            : R.string.settings_menu_lower_priority);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settingsmainmenu, menu);
        return super.onCreateOptionsMenu(menu);
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
        seekbar.setOnSeekBarChangeListener(mSizeChangeListener);
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
        if (( ! builtInProfilesAdded()) || ( ! cottonCandyProfileAdded())) {
            new ProfileLoader().execute(new Void[] { });
        }
    }

    private void reloadSettings() {
        mViews.mSizeSeekBar.setProgress(MIN_SIZE + (SIZE_STEP_COUNT - Math.max(0, mEffect.getSize())));

        mViews.mXMoveSpeedSeekBar.setProgress(TOTAL_SPEED_STEPS - (mEffect.getXMoveSpeed() + SPEED_DISPLAY_OFFSET));
        mViews.mYMoveSpeedSeekBar.setProgress(mEffect.getYMoveSpeed() + SPEED_DISPLAY_OFFSET);
        mViews.mXPulseSpeedSeekBar.setProgress(mEffect.getXPulseSpeed() + SPEED_DISPLAY_OFFSET);
        mViews.mYPulseSpeedSeekBar.setProgress(mEffect.getYPulseSpeed() + SPEED_DISPLAY_OFFSET);

        mViews.mRedAmountSeekBar.setProgress(mEffect.getRedAmount());
        mViews.mRedIntensitySeekBar.setProgress(mEffect.getRedIntensity());
        mViews.mRedWavelengthSeekBar.setProgress(mEffect.getRedWavelength());

        mViews.mGreenAmountSeekBar.setProgress(mEffect.getGreenAmount());
        mViews.mGreenIntensitySeekBar.setProgress(mEffect.getGreenIntensity());
        mViews.mGreenWavelengthSeekBar.setProgress(mEffect.getGreenWavelength());

        mViews.mBlueAmountSeekBar.setProgress(mEffect.getBlueAmount());
        mViews.mBlueIntensitySeekBar.setProgress(mEffect.getBlueIntensity());
        mViews.mBlueWavelengthSeekBar.setProgress(mEffect.getBlueWavelength());
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
        if (mViewState == VIEW_STATE_BUILDER) {
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
        if ((name == null) || (name.length() == 0)) {
            showInvalidNameDialog();
            return null;
        }

        return name;
    }

    private void flipToLibrary() {
        mViews.mLibraryButton.setVisibility(View.GONE);
        mViews.mEditorButton.setVisibility(View.VISIBLE);
        mViews.mFlipper.showPrevious();
        mViewState = VIEW_STATE_LIBRARY;
    }

    private void flipToBuilder() {
        mViews.mLibraryButton.setVisibility(View.VISIBLE);
        mViews.mEditorButton.setVisibility(View.GONE);
        mViews.mFlipper.showNext();
        mViewState = VIEW_STATE_BUILDER;
    }

    private boolean isLowPriorityEnabled() {
        return mPrefs.getBoolean(getString(R.string.pref_low_priority), true);
    }

    private void setLowPriorityEnabled(boolean enabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(getString(R.string.pref_low_priority), enabled);
        editor.commit();
        sendBroadcast(new Intent(ACTION_SETTINGS_PRIORITY_CHANGED));
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

        editor.commit();

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
                flipToBuilder();
            }
        };

    private DialogInterface.OnCancelListener mSaveProfileDialogCancelListener =
        new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                flipToBuilder();
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
            flipToBuilder();
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

    private OnSeekBarChangeListener mSizeChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (seekBar == mViews.mSizeSeekBar) {
                mEffect.setSize(MIN_SIZE + (SIZE_STEP_COUNT - progress));
            }
            else if (seekBar == mViews.mXMoveSpeedSeekBar) {
                mEffect.setXMoveSpeed(MIN_MOVE - (progress - SPEED_DISPLAY_OFFSET));
            }
            else if (seekBar == mViews.mYMoveSpeedSeekBar) {
                mEffect.setYMoveSpeed(MIN_MOVE - (progress - SPEED_DISPLAY_OFFSET));
            }
            else if (seekBar == mViews.mXPulseSpeedSeekBar) {
                mEffect.setXPulseSpeed(progress - SPEED_DISPLAY_OFFSET);
            }
            else if (seekBar == mViews.mYPulseSpeedSeekBar) {
                mEffect.setYPulseSpeed(progress - SPEED_DISPLAY_OFFSET);
            }
            else if (seekBar == mViews.mRedAmountSeekBar) {
                mEffect.setRedAmount(progress);
            }
            else if (seekBar == mViews.mRedIntensitySeekBar) {
                mEffect.setRedIntensity(progress);
            }
            else if (seekBar == mViews.mRedWavelengthSeekBar) {
                mEffect.setRedWavelength(progress);
            }
            else if (seekBar == mViews.mGreenAmountSeekBar) {
                mEffect.setGreenAmount(progress);
            }
            else if (seekBar == mViews.mGreenIntensitySeekBar) {
                mEffect.setGreenIntensity(progress);
            }
            else if (seekBar == mViews.mGreenWavelengthSeekBar) {
                mEffect.setGreenWavelength(progress);
            }
            else if (seekBar == mViews.mBlueAmountSeekBar) {
                mEffect.setBlueAmount(progress);
            }
            else if (seekBar == mViews.mBlueIntensitySeekBar) {
                mEffect.setBlueIntensity(progress);
            }
            else if (seekBar == mViews.mBlueWavelengthSeekBar) {
                mEffect.setBlueWavelength(progress);
            }
        }
    };

    private class ProfileLoader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if ( ! mPaused && mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            reloadProfiles();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if ( ! mPaused) {
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
                if ( ! builtInProfilesAdded()) {
                    Log.i(TAG, "Profiles don't exist, creating...");

                    if (mProfileLibrary.addBuiltInProfiles()) {
                        setPreferenceBoolean(
                            R.string.pref_built_in_profiles_added);

                        Log.i(TAG, "Profiles don't exist, created!");
                    }
                }

                if ( ! cottonCandyProfileAdded()) {
                    String name = "cotton candy";

                    String values = "{" +
                        "\"pref_red_amount\": 120," +
                        "\"pref_speed4\": 1," +
                        "\"pref_speed3\": 4," +
                        "\"pref_blue_intensity\": 80," +
                        "\"pref_green_amount\": 40," +
                        "\"pref_red_wavelength\": 40," +
                        "\"pref_size\": 1," +
                        "\"pref_red_intensity\": 20," +
                        "\"pref_speed2\": 1," +
                        "\"pref_blue_amount\": 120," +
                        "\"pref_green_wavelength\": 40," +
                        "\"pref_speed1\": 4," +
                        "\"pref_green_intensity\": 40," +
                        "\"pref_blue_wavelength\": 120" +
                    "}";

                    if (mProfileLibrary.addProfile(name, values) != -1) {
                        setPreferenceBoolean(R.string.pref_cotton_candy_added);
                    }
                }
            }

            return null;
        }

        protected void setPreferenceBoolean(int id) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(getString(id), true);
            editor.commit();
        }
    }
}
