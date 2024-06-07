package sound.wave.kilobyte;

import android.animation.ValueAnimator;
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
import android.Manifest;
import android.app.Activity;

import java.io.IOException;

public class WavePlayerView extends View {
		private Paint[] paints;
		private float[][] magnitudes;
		private MediaPlayer mediaPlayer;
		private Visualizer visualizer;
		private int waveLength = 500;
		private float waveSpeed;
		private boolean wave2Visible;
		private boolean isPaused = false;
		private int pausedPosition = 0;
		private ValueAnimator valueAnimator;
		private Handler handler;
		private Runnable updateCurrentTimeTask;
		private OnAudioProgressListener onAudioProgressListener;
		private OnAudioCompleteListener onAudioCompleteListener;
		private Handler backgroundHandler;
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
				handlerThread = new HandlerThread("VisualizerThread");
				handlerThread.start();
				backgroundHandler = new Handler(handlerThread.getLooper());
				
				updateCurrentTimeTask = new Runnable() {
						@Override
						public void run() {
								if (mediaPlayer != null && mediaPlayer.isPlaying() && onAudioProgressListener != null) {
										int currentTime = mediaPlayer.getCurrentPosition();
										onAudioProgressListener.onProgress(currentTime);
										handler.postDelayed(this, 50);
								}
						}
				};
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
		
		public void setOnAudioProgressListener(OnAudioProgressListener listener) {
				this.onAudioProgressListener = listener;
		}
		
		public void setOnAudioCompleteListener(OnAudioCompleteListener listener) {
				this.onAudioCompleteListener = listener;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
				super.onDraw(canvas);
				if (magnitudes != null) {
						float width = getWidth();
						float height = getHeight();
						float centerY = height / 2;
						float scaleX = (width / (float) waveLength) * waveSpeed;
						
						for (int j = 0; j < magnitudes.length; j++) {
								if (magnitudes[j] != null && (j == 0 || wave2Visible)) {
										Path path = new Path();
										path.moveTo(0, centerY);
										
										for (int i = 0; i < magnitudes[j].length; i++) {
												float x = i * scaleX;
												float y = centerY + (magnitudes[j][i] * centerY);
												path.lineTo(x, y);
										}
										
										canvas.drawPath(path, paints[j]);
								}
						}
				}
		}
		
		public void playAudioWithWave(int audioResId) {
				
				release();
				mediaPlayer = MediaPlayer.create(getContext(), audioResId);
				if (mediaPlayer != null) {
						mediaPlayer.setOnPreparedListener(mp -> {
								setupVisualizer();
								setPlaybackSpeed(playbackSpeed); 
								mediaPlayer.start();
								animateWave(true);
								handler.post(updateCurrentTimeTask);
						});
						
						mediaPlayer.setOnCompletionListener(mp -> {
								if (visualizer != null) {
										visualizer.release();
								}
								animateWave(false);
								handler.removeCallbacks(updateCurrentTimeTask);
								if (onAudioCompleteListener != null) {
										onAudioCompleteListener.onComplete();
								}
						});
				}
		}
		
		public void playAudioWithWaveFromPath(String path) {
				if (path != null){
						release();
						mediaPlayer = new MediaPlayer();
						try {
								mediaPlayer.setDataSource(path);
								mediaPlayer.prepareAsync();
								mediaPlayer.setOnPreparedListener(mp -> {
										setupVisualizer();
										setPlaybackSpeed(playbackSpeed); 
										mediaPlayer.start();
										animateWave(true);
										handler.post(updateCurrentTimeTask);
								});
								
								mediaPlayer.setOnCompletionListener(mp -> {
										if (visualizer != null) {
												visualizer.release();
										}
										animateWave(false);
										handler.removeCallbacks(updateCurrentTimeTask);
										if (onAudioCompleteListener != null) {
												onAudioCompleteListener.onComplete();
										}
								});
						} catch (IOException e) {
								e.printStackTrace();
						}
				}else{
						
				}
		}
		
		public void pauseAudioWithWave() {
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
						pausedPosition = mediaPlayer.getCurrentPosition();
						mediaPlayer.pause();
						isPaused = true;
						animateWave(false);
						handler.removeCallbacks(updateCurrentTimeTask);
				}
		}
		
		public void resumeAudioWithWave() {
				if (mediaPlayer != null && isPaused) {
						mediaPlayer.seekTo(pausedPosition);
						setPlaybackSpeed(playbackSpeed); 
						mediaPlayer.start();
						isPaused = false;
						animateWave(true);
						handler.post(updateCurrentTimeTask);
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
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
						mediaPlayer.stop();
				}
				animateWave(false);
				handler.removeCallbacks(updateCurrentTimeTask);
		}
		
		private void setupVisualizer() {
				if (mediaPlayer != null && mediaPlayer.getAudioSessionId() != -1) {
						
						//if (visualizer != null) {
						//visualizer.release();
						//}
						
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
								e.printStackTrace();
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
				handler.removeCallbacks(updateCurrentTimeTask);
		}
		
		@Override
		protected void onDetachedFromWindow() {
				super.onDetachedFromWindow();
				release();
				if (handlerThread != null) {
						handlerThread.quitSafely();
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
		@Override
		public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				switch (requestCode) {
						case REQUEST_RECORD_AUDIO_PERMISSION:
						permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
						break;
				}
				if (!permissionToRecordAccepted) ((Activity) getContext()).finish();
		}
}
