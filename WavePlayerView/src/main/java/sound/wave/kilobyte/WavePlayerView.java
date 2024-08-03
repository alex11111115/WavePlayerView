package sound.wave.kilobyte;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class WavePlayerView extends View {
    private Paint[] paints;
    private float[][] magnitudes;
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private Handler handler;
    private int waveLength = 500;
    private float waveSpeed;
    private boolean wave2Visible;
    private boolean isPaused = false;
    private int pausedPosition = 0;
    private ValueAnimator valueAnimator;
    private WeakReference<Handler> handlerRef;
    private Runnable updateCurrentTimeTask;
    private OnAudioProgressListener onAudioProgressListener;
    private OnAudioCompleteListener onAudioCompleteListener;
    private WeakReference<Handler> backgroundHandlerRef;
    private HandlerThread handlerThread;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private boolean isListening = false;
    private int bufferSize;
    private static final int SAMPLE_RATE = 44100;
    private String playbackSpeed = "1.0";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private float wave1Alpha = 1.0f;
    private float wave2Alpha = 1.0f;
    private static final int POINTS_PER_WAVE = 200; // زيادة عدد النقاط
    private float[] smoothedMagnitudes;

    public WavePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveView, 0, 0);

        int waveColor1 = a.getColor(R.styleable.WaveView_waveColor1, 0xFF8e8de5);
        float waveStrokeWidth1 = a.getDimension(R.styleable.WaveView_waveStrokeWidth1, 5.0f);
        int waveColor2 = a.getColor(R.styleable.WaveView_waveColor2, 0xFFe8d8fb);
        float waveStrokeWidth2 = a.getDimension(R.styleable.WaveView_waveStrokeWidth2, 5.0f);
        waveSpeed = a.getFloat(R.styleable.WaveView_waveSpeed, 0.5f);
        wave2Visible = a.getBoolean(R.styleable.WaveView_wave2Visible, true);

        paints = new Paint[2];
        paints[0] = new Paint();
        paints[0].setColor(waveColor1);
        paints[0].setStrokeWidth(waveStrokeWidth1);
        paints[0].setStyle(Paint.Style.STROKE);
        paints[0].setAntiAlias(true);
        paints[0].setShadowLayer(10, 0, 0, waveColor1);

        paints[1] = new Paint();
        paints[1].setColor(waveColor2);
        paints[1].setStrokeWidth(waveStrokeWidth2);
        paints[1].setStyle(Paint.Style.STROKE);
        paints[1].setAntiAlias(true);
        paints[1].setShadowLayer(10, 0, 0, waveColor2);

        magnitudes = new float[2][];
        a.recycle();

        handler = new Handler(Looper.getMainLooper());
        handlerRef = new WeakReference<>(new Handler(Looper.getMainLooper()));
        handlerThread = new HandlerThread("VisualizerThread");
        handlerThread.start();
        backgroundHandlerRef = new WeakReference<>(new Handler(handlerThread.getLooper()));

        updateCurrentTimeTask = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && onAudioProgressListener != null) {
                    int currentTime = mediaPlayer.getCurrentPosition();
                    onAudioProgressListener.onProgress(currentTime);
                    Handler handler = handlerRef.get();
                    if (handler != null) {
                        handler.postDelayed(this, 50);
                    }
                }
            }
        };

        // Enable hardware acceleration
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void updateMagnitudes(float[][] magnitudes) {
        this.magnitudes = magnitudes;
        invalidate();
    }

    public void setWaveProperties(int color1, float strokeWidth1, int color2, float strokeWidth2, float speed) {
        paints[0].setColor(color1);
        paints[0].setStrokeWidth(strokeWidth1);
        paints[0].setShadowLayer(10, 0, 0, color1);

        paints[1].setColor(color2);
        paints[1].setStrokeWidth(strokeWidth2);
        paints[1].setShadowLayer(10, 0, 0, color2);

        waveSpeed = speed;
        invalidate();
    }

    public void setWave2Visible(boolean visible) {
        wave2Visible = visible;
        invalidate();
    }

    public void setWave1Alpha(float alpha) {
        wave1Alpha = alpha;
        invalidate();
    }

    public void setWave2Alpha(float alpha) {
        wave2Alpha = alpha;
        invalidate();
    }

    public void setOnAudioProgressListener(OnAudioProgressListener listener) {
        this.onAudioProgressListener = listener;
    }

    public void setOnAudioCompleteListener(OnAudioCompleteListener listener) {
        this.onAudioCompleteListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (magnitudes != null && magnitudes.length > 0 && magnitudes[0] != null) {
            float width = getWidth();
            float height = getHeight();
            float centerY = height / 2;
            float scaleX = width / (float) (POINTS_PER_WAVE - 1);

            Path path = new Path();

            for (int j = 0; j < magnitudes.length; j++) {
                if (magnitudes[j] != null && (j == 0 || wave2Visible)) {
                    smoothedMagnitudes = smoothMagnitudes(magnitudes[j]);
                    path.reset();

                    for (int i = 0; i < POINTS_PER_WAVE; i++) {
                        float x = i * scaleX;
                        float y = centerY + (smoothedMagnitudes[i] * centerY);
                        
                        if (i == 0) {
                            path.moveTo(x, y);
                        } else {
                            float prevX = (i - 1) * scaleX;
                            float prevY = centerY + (smoothedMagnitudes[i - 1] * centerY);
                            float midX = (prevX + x) / 2;
                            path.quadTo(midX, prevY, x, y);
                        }
                    }

                    int alpha = (int) (255 * (j == 0 ? wave1Alpha : wave2Alpha));
                    paints[j].setAlpha(alpha);

                    canvas.drawPath(path, paints[j]);
                }
            }
        }
    }

    private float[] smoothMagnitudes(float[] original) {
        Log.d("WavePlayerView", "Original length: " + (original != null ? original.length : "null"));
        if (original == null || original.length == 0) {
            return new float[POINTS_PER_WAVE];
        }

        float[] smoothed = new float[POINTS_PER_WAVE];
        int originalLength = original.length;
        
        for (int i = 0; i < POINTS_PER_WAVE; i++) {
            float sum = 0;
            int count = 0;
            int start = Math.max(0, (i * originalLength / POINTS_PER_WAVE) - 2);
            int end = Math.min(originalLength - 1, (i * originalLength / POINTS_PER_WAVE) + 2);
            
            for (int j = start; j <= end; j++) {
                sum += original[j];
                count++;
            }
            
            smoothed[i] = count > 0 ? sum / count : 0;
        }
        
        return smoothed;
    }
    
    private void setupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(mp -> {
                setupVisualizer();
                setPlaybackSpeed(playbackSpeed);
                mediaPlayer.start();
                animateWave(true);
                startProgressUpdates();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                if (visualizer != null) {
                    visualizer.release();
                }
                animateWave(false);
                stopProgressUpdates();
                if (onAudioCompleteListener != null) {
                    onAudioCompleteListener.onComplete();
                }
            });
        }
    }

    private void startProgressUpdates() {
        if (handler != null) {
            handler.post(updateProgressRunnable);
        }
    }

    private void stopProgressUpdates() {
        if (handler != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (onAudioProgressListener != null) {
                    onAudioProgressListener.onProgress(currentPosition);
                }
                handler.postDelayed(this, 100); // تحديث كل 100 مللي ثانية
            }
        }
    };

    public void playAudioWithWave(int audioResId) {
        release();
        try {
            mediaPlayer = MediaPlayer.create(getContext(), audioResId);
            setupMediaPlayer();
        } catch (Exception e) {
            Log.e("WavePlayerView", "Error playing audio: " + e.getMessage());
        }
    }

    public void playAudioWithWaveFromPath(String path) {
        if (path != null) {
            release();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
                setupMediaPlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	public void pauseAudioWithWave() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
        pausedPosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();
        isPaused = true;
        animateWave(false);
        if (handler != null) {
            handler.removeCallbacks(updateCurrentTimeTask);
        }
    }
}
	
	public void resumeAudioWithWave() {
    if (mediaPlayer != null && isPaused) {
        mediaPlayer.seekTo(pausedPosition);
        setPlaybackSpeed(playbackSpeed);
        mediaPlayer.start();
        isPaused = false;
        animateWave(true);
        if (handler != null) {
            handler.post(updateCurrentTimeTask);
        }
    }
}
	
	public void seekToPosition(int pos) {
		if (mediaPlayer != null && isPaused) {
			mediaPlayer.seekTo(pos);
			setPlaybackSpeed(playbackSpeed); 
			mediaPlayer.start();
			isPaused = false;
			animateWave(true);
			handler.post(updateCurrentTimeTask);
		}
	}
	
	public void stopAudioWithWave() {
    if (mediaPlayer != null) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }
    
    if (visualizer != null) {
        visualizer.release();
        visualizer = null;
    }
    
    animateWave(false);
    
    if (handler != null) {
        handler.removeCallbacks(updateCurrentTimeTask);
    }
    
    isPaused = false;
    pausedPosition = 0;
    
    if (onAudioCompleteListener != null) {
        onAudioCompleteListener.onComplete();
    }
}
	
	private void setupVisualizer() {
        if (mediaPlayer != null && mediaPlayer.getAudioSessionId() != -1) {
            try {
                visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                        float[] magnitudes1 = new float[waveform.length];
                        float[] magnitudes2 = new float[waveform.length];
                        for (int i = 0; i < waveform.length; i++) {
                            float magnitude = ((waveform[i] & 0xFF) - 128) / 128f;
                            magnitudes1[i] = magnitude;
                            magnitudes2[i] = magnitude * 0.5f;
                        }
                        post(() -> updateMagnitudes(new float[][]{magnitudes1, magnitudes2}));
                    }

                    @Override
                    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                        // Not used here
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
                visualizer.setEnabled(true);
            } catch (Exception e) {
                Log.e("WavePlayerView", "Error setting up visualizer: " + e.getMessage());
                // Handle the error (e.g., show a message to the user)
            }
        }
    }
	
	private void animateWave(boolean start) {
		if (valueAnimator != null) {
			valueAnimator.cancel();
		}
		
		if (start) {
			valueAnimator = ValueAnimator.ofFloat(0f, 1f);
			valueAnimator.setDuration(450); 
			valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
			valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
			valueAnimator.addUpdateListener(animation -> invalidate());
			valueAnimator.start();
		} else {
			invalidate();
		}
	}
	
	public void release() {
    if (mediaPlayer != null) {
        mediaPlayer.release();
        mediaPlayer = null;
    }
    if (visualizer != null) {
        visualizer.release();
        visualizer = null;
    }
    if (audioRecord != null) {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }
    if (valueAnimator != null) {
        valueAnimator.cancel();
        valueAnimator = null;
    }
    if (handler != null) {
        handler.removeCallbacks(updateCurrentTimeTask);
    }
}

    @Override
protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    release();
    if (handler != null) {
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
    if (handlerThread != null) {
        handlerThread.quitSafely();
        handlerThread = null;
    }
}
	
	public int getAudioDuration() {
		if (mediaPlayer != null) {
			return mediaPlayer.getDuration();
		}
		return 0;
	}
	
	public int getCurrentAudioPosition() {
		if (mediaPlayer != null) {
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}
	
	public void setPlaybackSpeed(String speedStr) {
		if (speedStr != null && mediaPlayer != null){
			try {
				float speed = Float.parseFloat(speedStr);
				
				if (speed >= 0.2f && speed <= 6.0f) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
						PlaybackParams playbackParams = new PlaybackParams();
						playbackParams.setSpeed(speed);
						mediaPlayer.setPlaybackParams(playbackParams);
					} else {
						playbackSpeed = String.valueOf(speed);
					}
				} else {
					Log.e("WaveView", "Speed must be between 0.2 and 6.0");
				}
			} catch (NumberFormatException e) {
				Log.e("WaveView", "Invalid speed format");
			}
		}
	}
	
	public void setVolume(float leftVolume, float rightVolume) {
		if (mediaPlayer != null) {
			mediaPlayer.setVolume(leftVolume, rightVolume);
		}
	}
	
	public void setLooping(boolean looping) {
		if (mediaPlayer != null) {
			mediaPlayer.setLooping(looping);
		}
	}
	
	public interface OnAudioProgressListener {
        void onProgress(int currentTime);
    }

    public interface OnAudioCompleteListener {
        void onComplete();
    }
	
	public void startListening() {
		release();
		if (isRecording || isListening) return;
		
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
			
			bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			audioRecord.startRecording();
		} else {
			// Handle the case where the permission is not granted
			ActivityCompat.requestPermissions((Activity) getContext(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
		}
		isListening = true;
		new Thread(() -> {
			short[] buffer = new short[bufferSize];
			while (isListening) {
				int read = audioRecord.read(buffer, 0, buffer.length);
				if (read > 0) {
					float[] magnitudes1 = new float[read];
					float[] magnitudes2 = new float[read];
					for (int i = 0; i < read; i++) {
						float magnitude = buffer[i] / 32768f;
						magnitudes1[i] = magnitude;
						magnitudes2[i] = magnitude * 0.5f;
					}
					post(() -> updateMagnitudes(new float[][]{magnitudes1, magnitudes2}));
				}
			}
		}).start();
	}
	
	public void stopListening() {
		if (!isListening) return;
		
		if (audioRecord != null){
			
			isListening = false;
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_RECORD_AUDIO_PERMISSION:
			permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
			break;
		}
		if (!permissionToRecordAccepted) ((Activity) getContext()).finish();
	}
}