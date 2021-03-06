package com.mythopc.nova.mispad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import co.mobiwise.library.MusicPlayerView;

public class DJPad extends AppCompatActivity implements View.OnClickListener, View.OnDragListener {

    Button song1Start, song1Stop, song2Start, song2Stop;
    TextView song1, song2;
    String r1, r2;
    SoundPool sp;
    MediaPlayer mediaPlayer1;
    MediaPlayer mediaPlayer2;
    MusicPlayerView mpv1;
    MusicPlayerView mpv2;
    AudioManager audioManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_djpad);




        Intent i = getIntent();
        r1 = i.getStringExtra("filename1");
        r2 = i.getStringExtra("filename2");

        mediaPlayer1 = new MediaPlayer();
        mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer1.setDataSource(r1);
            mediaPlayer1.prepare();
        } catch (IOException e) {
        }

        mediaPlayer2 = new MediaPlayer();
        mediaPlayer2.setAudioStreamType(AudioManager.STREAM_RING);
        try {
            mediaPlayer2.setDataSource(r2);
            mediaPlayer2.prepare();
        }
        catch (IOException e) {
        }


        mpv1 = (MusicPlayerView) findViewById(R.id.mpv1);
        mpv2 = (MusicPlayerView) findViewById(R.id.mpv2);

        mpv1.setTimeColor(Color.BLUE);
        mpv1.setCoverDrawable(R.drawable.mycover);




        mpv2.setTimeColor(Color.BLUE);
        mpv2.setCoverDrawable(R.drawable.cover2);


        mpv1.setOnClickListener(this);
        mpv2.setOnClickListener(this);

        mpv1.setOnDragListener(this);


        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        int maxVolume1 = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume1 = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl1 = (SeekBar)findViewById(R.id.volumn1);
        volControl1.setMax(maxVolume1);
        volControl1.setProgress(curVolume1);
        volControl1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });

        int maxVolume2 = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int curVolume2 = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        SeekBar volControl2 = (SeekBar)findViewById(R.id.volumn2);
        volControl2.setMax(maxVolume2);
        volControl2.setProgress(curVolume2);
        volControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, arg1, 0);
            }
        });







    }

    public void onClick(View view) {
        if (view == mpv1) {
            if (mpv1.isRotating()) {
                mediaPlayer1.pause();
                mpv1.stop();
            } else {
                mediaPlayer1.start();
                mpv1.start();
            }
        }

        if (view == mpv2) {
            if (mpv2.isRotating()) {
                mediaPlayer2.pause();
                mpv2.stop();
            } else {
                mediaPlayer2.start();
                mpv2.start();
            }
        }
    }

    public void onBackPressed() {
        finish();
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        Toast.makeText(getApplicationContext(),"I am dragging",Toast.LENGTH_LONG).show();
        return false;
    }
}
