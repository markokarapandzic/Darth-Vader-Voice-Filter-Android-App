package com.example.darthvadersoundfilterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.media.audiofx.BassBoost;
import android.media.audiofx.PresetReverb;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 201;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 202;
    private final String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.pcm";
    private MediaRecorder mRecorder = null;
    private MediaPlayer mMediaPlayer;
    private Button recordVoice, playVoice;
    private Boolean permissionToRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO_PERMISSION);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);

        getComponentsFromView();
        setTouchListenerForRecordButton();
        setClickListenerForPlayButton();
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
            case REQUEST_RECORD_AUDIO_PERMISSION:
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!permissionToRecord) finish();
    }

    public void getComponentsFromView() {
        recordVoice = findViewById(R.id.buRecordVoice);
        playVoice = findViewById(R.id.buPlayVoice);
        playVoice.setEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setTouchListenerForRecordButton() {
        recordVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    try {
                        startRecording();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    stopRecording();
                return true;
            }
        });
    }

    public void setClickListenerForPlayButton() {
        playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVoice();
            }
        });
    }

    public void startRecording() throws IOException {
        configMediaRecorder();
        startMediaRecorder();
        Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
    }

    public void configMediaRecorder() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(outputFile);
    }

    public void startMediaRecorder() throws IOException {
        mRecorder.prepare();
        mRecorder.start();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        playVoice.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Recording Finished", Toast.LENGTH_SHORT).show();
    }

    public void playVoice() {
        try {
            loadVoiceWithEffectAndPlayMediaPlayer();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void loadVoiceWithEffectAndPlayMediaPlayer() throws IOException {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setDataSource(outputFile);
        configureParams();
        configureReverb();
        configureBassBoost();

        try {
            playMediaPlayer();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void configureParams() {
        PlaybackParams params;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            params = new PlaybackParams();
            params.setPitch(0.70f);
            mMediaPlayer.setPlaybackParams(params);
        } else {
            Toast.makeText(getApplicationContext(), "Your Phone Does't Support Audio Modifications", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void configureReverb() {
        PresetReverb mReverb = new PresetReverb(0, mMediaPlayer.getAudioSessionId());
        mReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
        mReverb.setEnabled(true);
    }

    public void configureBassBoost() {
        BassBoost mBass = new BassBoost(0, mMediaPlayer.getAudioSessionId());
        mBass.setStrength((short) 1000);
        mBass.setEnabled(true);
    }

    public void playMediaPlayer() throws IOException {
        mMediaPlayer.setAuxEffectSendLevel(1.0f);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }
}
