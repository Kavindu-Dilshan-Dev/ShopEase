package com.kavindu.shopeaseapp.activities;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kavindu.shopeaseapp.databinding.ActivityMultimediaBinding;

import java.io.IOException;

public class MultimediaActivity extends AppCompatActivity {

    private ActivityMultimediaBinding binding;
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private String recordedFilePath;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultimediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Multimedia");
        }

        recordedFilePath = getExternalCacheDir().getAbsolutePath() + "/shopease_audio.3gp";

        binding.btnRecord.setOnClickListener(v -> toggleRecording());
        binding.btnPlay.setOnClickListener(v -> togglePlayback());
        binding.btnStopPlay.setOnClickListener(v -> stopPlayback());

        // VideoView with sample promo video
        binding.videoView.setVideoURI(Uri.parse(
                "android.resource://" + getPackageName() + "/raw/promo_video"));
        binding.videoView.setMediaController(new MediaController(this));

        binding.btnPlayVideo.setOnClickListener(v -> {
            if (!binding.videoView.isPlaying()) binding.videoView.start();
            else binding.videoView.pause();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void toggleRecording() {
        if (!isRecording) startRecording();
        else stopRecording();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordedFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            binding.btnRecord.setText("⏹ Stop Recording");
            binding.tvStatus.setText("🔴 Recording...");
        } catch (IOException e) {
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        isRecording = false;
        binding.btnRecord.setText("🎙 Record Audio");
        binding.tvStatus.setText("Recording saved");
        binding.btnPlay.setEnabled(true);
    }

    private void togglePlayback() {
        if (!isPlaying) startPlayback();
        else pausePlayback();
    }

    private void startPlayback() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordedFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            binding.btnPlay.setText("⏸ Pause");
            binding.tvStatus.setText("▶ Playing...");
            updateSeekBar();
            mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
        } catch (IOException e) {
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            binding.btnPlay.setText("▶ Play");
            binding.tvStatus.setText("Paused");
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        binding.btnPlay.setText("▶ Play Audio");
        binding.tvStatus.setText("Stopped");
        binding.seekBar.setProgress(0);
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            binding.seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(() -> {
                if (mediaPlayer != null) {
                    binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    updateSeekBar();
                }
            }, 500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}