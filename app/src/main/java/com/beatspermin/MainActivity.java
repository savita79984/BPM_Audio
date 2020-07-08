package com.beatspermin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView tvAudioFile;
    private EditText etBmp;
    private Button btnChangeBmp, btnStart, btnStop;
    private MediaPlayer mp;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        tvAudioFile = findViewById(R.id.tvAudioFile);
        btnChangeBmp = findViewById(R.id.btnChangeBmp);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        etBmp = findViewById(R.id.etBmp);

        tvAudioFile.setOnClickListener(this::OnClicked);
        btnChangeBmp.setOnClickListener(this::OnClicked);
        btnStart.setOnClickListener(this::OnClicked);
        btnStop.setOnClickListener(this::OnClicked);

        //creating media player
        mp = new MediaPlayer();
    }

    private void OnClicked(View view) {
        switch (view.getId()) {
            case R.id.tvAudioFile:
                checkPermission(103, Manifest.permission.READ_EXTERNAL_STORAGE);
                break;
            case R.id.btnChangeBmp:
//                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(Float.parseFloat(etBmp.getText().toString())));
//                mp.start();

                if (etBmp.getText() != null && Integer.parseInt(etBmp.getText().toString()) > 60) {
                    Toast.makeText(mContext, "Value should be between 0 to 60", Toast.LENGTH_SHORT).show();
                } else {
                    float playbackSpeed = 1.5f;
                    SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
                    int soundId = soundPool.load(filePath, 1);
                    AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    assert mgr != null;
                    final float volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
                            soundPool.play(soundId, volume, volume, 1, 0, playbackSpeed);
                        }
                    });
                }
                break;
            case R.id.btnStart:
                mp.start();
                break;
            case R.id.btnStop:
                mp.stop();
                break;
        }
    }

    AudioTrack audio;

    private void changeBmp() {
        //read track from file
        File file = new File(filePath);
        int size = (int) file.length();

        audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                size, //size of pcm file to read in bytes
                AudioTrack.MODE_STATIC);

        byte[] data = new byte[size];

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(data, 0, size);
            fileInputStream.close();

            audio.write(data, 0, data.length);
            if (etBmp.getText() != null)
                changeSpeed(Double.parseDouble(etBmp.getText().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //change playback speed by factor
    void changeSpeed(double factor) {
        audio.setPlaybackRate((int) (audio.getPlaybackRate() * factor));
    }

    public void checkPermission(int requestCode, String permission) {
        switch (requestCode) {
            case 103:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/*"});
                startActivityForResult(i, requestCode);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) return;
        if (requestCode == 103) {
            setFilePath(resultCode, data, 103);
        }
    }

    private void setFilePath(int resultCode, Intent data, int mode) {
        switch (mode) {
            case 103:
                try {
                    Uri selectedImage = data.getData();
                    Log.e("PATH IS --->   ", selectedImage.toString());
                    String[] filePathColumn = new String[]{MediaStore.MediaColumns.DATA};

                    Cursor cursor = mContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                        Log.e("filePath: AUDIO ---->>", filePath);
                        cursor.close();
                        File masterAudioFile = new File(filePath);
                        tvAudioFile.setText("" + masterAudioFile.getName().toString());

                        setMediaPlayer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private String filePath = "";

    private void setMediaPlayer() {
        try {
            mp.setDataSource(filePath);
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
