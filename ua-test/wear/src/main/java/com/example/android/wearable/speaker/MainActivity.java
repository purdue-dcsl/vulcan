/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.speaker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * We first get the required permission to use the MIC. If it is granted, then we continue with
 * the application and present the UI with three icons: a MIC icon (if pressed, user can record up
 * to 10 seconds), a Play icon (if clicked, it wil playback the recorded audio file) and a music
 * note icon (if clicked, it plays an MP3 file that is included in the app).
 */
public class MainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider,
        UIAnimation.UIStateListener,
        SoundRecorder.OnVoicePlaybackStateChangedListener,
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final long COUNT_DOWN_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long MILLIS_IN_SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final String VOICE_FILE_NAME = "audiorecord.pcm";

    private MediaPlayer mMediaPlayer;
    private AppState mState = AppState.READY;
    private UIAnimation.UIState mUiState = UIAnimation.UIState.HOME;
    private SoundRecorder mSoundRecorder;

    private RelativeLayout mOuterCircle;
    private View mInnerCircle;

    private UIAnimation mUIAnimation;
    private ProgressBar mProgressBar;
    private CountDownTimer mCountDownTimer;

    private TextView mDate;
    private Calendar mCalDate;

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in
     * ambient mode.
     */
    private AmbientModeSupport.AmbientController mAmbientController;

    enum AppState {
        READY, PLAY_OPTION_1, PLAY_OPTION_2
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mOuterCircle = findViewById(R.id.outer_circle);
        mInnerCircle = findViewById(R.id.inner_circle);

        mProgressBar = findViewById(R.id.progress_bar);

        mDate = findViewById(R.id.textView);
        setDate(Calendar.getInstance());

        // Enables Ambient mode.
        mAmbientController = AmbientModeSupport.attach(this);
    }

    private void setProgressBar(long progressInMillis) {
        mProgressBar.setProgress((int) (progressInMillis / MILLIS_IN_SECOND));
    }

    @Override
    public void onUIStateChanged(UIAnimation.UIState state) {
        Log.d(TAG, "UI State is: " + state);
        if (mUiState == state) {
            return;
        }
        switch (state) {
            case OPTION_1:
                mState = AppState.PLAY_OPTION_1;
                mUiState = state;
                viewEvent(0);
                // createEvent("Event", System.currentTimeMillis(),
                //        System.currentTimeMillis() + 3600 * 1000);
                break;
            case OPTION_2:
                mState = AppState.PLAY_OPTION_2;
                mUiState = state;
                viewEvent(Constant.POC_AU_DELAY);
                break;
            case HOME:
                switch (mState) {
                    case PLAY_OPTION_1:
                        mState = AppState.READY;
                        mUiState = state;
                        break;
                    case PLAY_OPTION_2:
                        mState = AppState.READY;
                        mUiState = state;
                        break;
                }
                break;
        }
    }

    private void createEvent(String title, long startDateMillis, long endDateMillis) {
        Log.d("xxx", "Create calendar entry");

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDateMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDateMillis)
                .putExtra(CalendarContract.Events.TITLE, "Yoga")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
        startActivity(intent);
    }

    /**
     * Creates an event in the Calendar
     * @see: https://developer.android.com/guide/topics/providers/calendar-provider.html#intent-insert
     */
    private void viewEvent(long delay) {

        long start = System.currentTimeMillis();
        Toast.makeText(this, "Creating event!", Toast.LENGTH_LONG).show();

        // A date-time specified in milliseconds since the epoch.
        long startMillis = mCalDate.getTimeInMillis();

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build())
                .putExtra(CalendarContract.Events.TITLE, "Yoga Class");

        // add delay
        if ( delay > 0 ) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }
        }

        Log.d(TAG, String.format(">> delay: %d time: %d", delay, System.currentTimeMillis() - start));
        startActivity(intent);

        // update date
        mCalDate.add(Calendar.DAY_OF_MONTH, 1);
        setDate(mCalDate);
    }

    /**
     * Checks the permission that this app needs and if it has not been granted, it will
     * prompt the user to grant it, otherwise it shuts down the app.
     */
    private void checkPermissions() {
        boolean recordAudioPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;

        if (recordAudioPermissionGranted) {
            start();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                // Permission has been denied before. At this point we should show a dialog to
                // user and explain why this permission is needed and direct him to go to the
                // Permissions settings for the app in the System settings. For this sample, we
                // simply exit to get to the important part.
                Toast.makeText(this, R.string.exiting_for_permissions, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Starts the main flow of the application.
     */
    private void start() {
        mSoundRecorder = new SoundRecorder(this, VOICE_FILE_NAME, this);
        int[] thumbResources = new int[] {R.id.option1, R.id.option2};
        ImageView[] thumbs = new ImageView[2];
        for(int i=0; i < 2; i++) {
            thumbs[i] = findViewById(thumbResources[i]);
        }
        View containerView = findViewById(R.id.container);
        ImageView expandedView = findViewById(R.id.expanded);
        int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mUIAnimation = new UIAnimation(containerView, thumbs, expandedView, animationDuration,
                this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPlaybackStopped() {
        mUIAnimation.transitionToHome();
        mUiState = UIAnimation.UIState.HOME;
        mState = AppState.READY;
    }

    /**
     * Determines if the wear device has a built-in speaker and if it is supported. Speaker, even if
     * physically present, is only supported in Android M+ on a wear device..
     */
    public final boolean speakerIsSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager packageManager = getPackageManager();
            // The results from AudioManager.getDevices can't be trusted unless the device
            // advertises FEATURE_AUDIO_OUTPUT.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false;
            }
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            Log.d(TAG, "onEnterAmbient() " + ambientDetails);

            // Changes views to grey scale.
            Context context = getApplicationContext();
            Resources resources = context.getResources();

            mOuterCircle.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.light_grey));
            mInnerCircle.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.grey_circle));

            mProgressBar.setProgressTintList(
                    resources.getColorStateList(R.color.white, context.getTheme()));
            mProgressBar.setProgressBackgroundTintList(
                    resources.getColorStateList(R.color.black, context.getTheme()));
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();

            Log.d(TAG, "onExitAmbient()");

            // Changes views to color.
            Context context = getApplicationContext();
            Resources resources = context.getResources();

            mOuterCircle.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.background_color));
            mInnerCircle.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.color_circle));

            mProgressBar.setProgressTintList(
                    resources.getColorStateList(R.color.progressbar_tint, context.getTheme()));
            mProgressBar.setProgressBackgroundTintList(
                    resources.getColorStateList(
                            R.color.progressbar_background_tint, context.getTheme()));
        }
    }

    /* ---------------------------------------------------------------------------
     * Date selection
     * ---------------------------------------------------------------------------
     */

    /**
     * This callback method invokes DatePickerFragment class and returns a calendar
     * view.
     * @param view
     */
    public void showDatePickerDialog(View view) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date");
    }

    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        // update text in view
        mCalDate = calendar;
        mDate.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, month, dayOfMonth);
        setDate(cal);
    }

    public static class DatePickerFragment extends DialogFragment {

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        }
    }


}
