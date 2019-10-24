package com.example.darthvadersoundfilterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    final String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.pcm";

    public static final String LOG_TAG = "MainActivity";
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 201;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 202;

    private MediaRecorder mRecorder = null;
    private SoundPool mSoundPool;
    private Equalizer mEqualizer;
    private BassBoost mBassBoost;
    Button recordVoice, playVoice;
    private Boolean permissionToRecord = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO_PERMISSION);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);

        recordVoice = findViewById(R.id.buRecordVoice);
        playVoice = findViewById(R.id.buPlayVoice);
        playVoice.setEnabled(false);

        // Set Listener to Check if the Button is Pressed Down
        recordVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    startRecording();
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    stopRecording();
                return true;
            }
        });

        playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVoice();
//                changeAudio();
            }
        });
    }

    // Recording START
    public void startRecording() {
        try {
            configMediaRecorder(outputFile);
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
    }

    public void stopRecording() {
        Log.d(LOG_TAG, "State of MediaRecorder: " + mRecorder);

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        playVoice.setEnabled(true);

        Toast.makeText(getApplicationContext(), "Recording Finished", Toast.LENGTH_SHORT).show();
    }
    // Recording END

    // Play Voice
    public void playVoice() {
        try {
            loadVoiceWithEffectAndPlayMediaPlayer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public void loadVoiceWithEffectAndPlay() {
//        final int soundID;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mSoundPool = new SoundPool.Builder()
//                .setMaxStreams(1)
//                .build();
//        } else {
//            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
//        }
//
//        soundID = mSoundPool.load(outputFile, 1);
//
//        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                mSoundPool.play(soundID, 1f, 1f, 1, 0, 0.75f);
//                Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void loadVoiceWithEffectAndPlayMediaPlayer() {
        MediaPlayer mMediaPlayer = new MediaPlayer();
        PlaybackParams params;

        try {
            mMediaPlayer.setDataSource(outputFile);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                params = new PlaybackParams();
                params.setPitch(0.70f);
                mMediaPlayer.setPlaybackParams(params);

                PresetReverb mReverb = new PresetReverb(0, mMediaPlayer.getAudioSessionId());
                mReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
                mReverb.setEnabled(true);

                BassBoost mBass = new BassBoost(0, mMediaPlayer.getAudioSessionId());
                mBass.setStrength((short) 1000);
                mBass.setEnabled(true);

                mMediaPlayer.setAuxEffectSendLevel(1.0f);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } else {
                Toast.makeText(getApplicationContext(), "Buy a New Phone", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void configMediaRecorder(String outputFile) {
        // Configure MediaRecorder
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(outputFile);
    }

    // Check for Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!permissionToRecord) finish();
    }
}
