package sound.wave.kilobyte;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.SeekBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
		
		private Handler handler = new Handler(Looper.getMainLooper());
		private String file = "";
		private double value = 0;
		private String playbackSpeed = "1.0";
		
		private WavePlayerView waveView;
		private EditText filePathEditText;
		private MaterialTextView currentTimeTextView;
		private SeekBar progressBar;
		private MaterialTextView durationTextView;
		
		private ActivityResultLauncher<Intent> activityResultLauncher;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.main);
				
				initialize();
				requestPermissions();
		}
		
		private void requestPermissions() {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
				} else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
				} else {
						initializeLogic();
				}
		}
		
		@Override
		public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						initializeLogic();
				} else if (requestCode == 9653 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						initializeLogic();
				}
		}
		
		private void initialize() {
				waveView = findViewById(R.id.waveView);
				filePathEditText = findViewById(R.id.edittext1);
				currentTimeTextView = findViewById(R.id.currentTimeTextView);
				progressBar = findViewById(R.id.progressbar1);
				durationTextView = findViewById(R.id.durationTextView);
				
				MaterialButton startListeningButton = findViewById(R.id.button2);
				startListeningButton.setOnClickListener(view -> waveView.startListening());
				
				MaterialButton stopListeningButton = findViewById(R.id.button1);
				stopListeningButton.setOnClickListener(view -> waveView.stopListening());
				
				MaterialButton pickFileButton = findViewById(R.id.pick);
				pickFileButton.setOnClickListener(view -> {
						if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
								ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
						} else {
								Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
								intent.setType("audio/*");
								intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
								activityResultLauncher.launch(intent);
						}
				});
				
				MaterialButton playWithFilePathButton = findViewById(R.id.play_with_file_path);
				playWithFilePathButton.setOnClickListener(view -> playAudioFromFile());
				
				MaterialButton playFromAppButton = findViewById(R.id.play_from_app);
				playFromAppButton.setOnClickListener(view -> playAudioFromApp());
				
				MaterialButton stopAudioButton = findViewById(R.id.stop);
				stopAudioButton.setOnClickListener(view -> stopAudio());
				
				MaterialButton resumeAudioButton = findViewById(R.id.resume);
				resumeAudioButton.setOnClickListener(view -> {
						waveView.resumeAudioWithWave();
						waveView.setPlaybackSpeed(playbackSpeed);
				});
				
				MaterialButton pauseAudioButton = findViewById(R.id.pause);
				pauseAudioButton.setOnClickListener(view -> waveView.pauseAudioWithWave());
				
				MaterialButton setPlaybackSpeedButton = findViewById(R.id.button4);
				setPlaybackSpeedButton.setOnClickListener(view -> {
						playbackSpeed = ((EditText) findViewById(R.id.edittext7)).getText().toString();
						waveView.setPlaybackSpeed(playbackSpeed);
				});
				
				MaterialButton updateWavePropertiesButton = findViewById(R.id.update);
				updateWavePropertiesButton.setOnClickListener(view -> updateWaveProperties());
				
				progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
								value = progress;
						}
						
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
								waveView.pauseAudioWithWave();
						}
						
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
								waveView.seekToPosition((int) value);
								waveView.setPlaybackSpeed(playbackSpeed);
						}
				});
				
				MaterialButton hideWave2Button = findViewById(R.id.hide_wave2);
				hideWave2Button.setOnClickListener(view -> waveView.setWave2Visible(false));
				
				MaterialButton showWave2Button = findViewById(R.id.show_wave2);
				showWave2Button.setOnClickListener(view -> waveView.setWave2Visible(true));
				
				activityResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
						if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
								Intent data = result.getData();
								ArrayList<String> filePaths = new ArrayList<>();
								if (data.getClipData() != null) {
										for (int i = 0; i < data.getClipData().getItemCount(); i++) {
												ClipData.Item item = data.getClipData().getItemAt(i);
												filePaths.add(FileUtil.convertUriToFilePath(getApplicationContext(), item.getUri()));
										}
								} else if (data.getData() != null) {
										filePaths.add(FileUtil.convertUriToFilePath(getApplicationContext(), data.getData()));
								}
								if (!filePaths.isEmpty()) {
										filePathEditText.setText(filePaths.get(0));
								}
						}
				}
				);
		}
		
		private void initializeLogic() {
				waveView.setOnAudioCompleteListener(() -> progressBar.setProgress(0));
				playbackSpeed = "1.0";
		}
		
		private void playAudioFromFile() {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
				} else {
						waveView.setOnAudioProgressListener(currentTime -> {
								String currentTimeStr = formatMilliseconds(currentTime);
								currentTimeTextView.setText(currentTimeStr);
								progressBar.setProgress(currentTime);
						});
						
						file = filePathEditText.getText().toString();
						waveView.playAudioWithWaveFromPath(file);
						handler.postDelayed(() -> {
								int duration = waveView.getAudioDuration();
								String durationStr = formatMilliseconds(duration);
								durationTextView.setText(durationStr);
								progressBar.setMax(duration);
						}, 50);
						
						try {
								waveView.setPlaybackSpeed(playbackSpeed);
						} catch (IllegalArgumentException e) {
								e.printStackTrace();
						}
				}
		}
		
		private void playAudioFromApp() {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
				} else {
						waveView.setOnAudioProgressListener(currentTime -> {
								String currentTimeStr = formatMilliseconds(currentTime);
								currentTimeTextView.setText(currentTimeStr);
								progressBar.setProgress(currentTime);
						});
						
						waveView.playAudioWithWave(R.raw.mus);
						handler.postDelayed(() -> {
								int duration = waveView.getAudioDuration();
								String durationStr = formatMilliseconds(duration);
								durationTextView.setText(durationStr);
								progressBar.setMax(duration);
						}, 1000);
						
						waveView.setPlaybackSpeed(playbackSpeed);
				}
		}
		
		private void updateWaveProperties() {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
				} else {
						String color1String = ((EditText) findViewById(R.id.edittext2)).getText().toString();
						String wave1WidthString = ((EditText) findViewById(R.id.edittext3)).getText().toString();
						String color2String = ((EditText) findViewById(R.id.edittext4)).getText().toString();
						String wave2WidthString = ((EditText) findViewById(R.id.edittext5)).getText().toString();
						String waveSpeedString = ((EditText) findViewById(R.id.edittext6)).getText().toString();
						
						if (!(color1String.isEmpty() && wave1WidthString.isEmpty() && color2String.isEmpty() && wave2WidthString.isEmpty() && waveSpeedString.isEmpty())) {
								try {
										int color1 = Color.parseColor(color1String);
										float wave1Width = Float.parseFloat(wave1WidthString);
										int color2 = Color.parseColor(color2String);
										float wave2Width = Float.parseFloat(wave2WidthString);
										float waveSpeed = Float.parseFloat(waveSpeedString);
										
										waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
								} catch (IllegalArgumentException e) {
										e.printStackTrace();
								}
						}
				}
		}
		
		private void stopAudio() {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
				} else {
						if (waveView != null) {
								waveView.stopAudioWithWave();
						}
				}
		}
		
		private String formatMilliseconds(int milliseconds) {
				int seconds = (milliseconds / 1000) % 60;
				int minutes = (milliseconds / (1000 * 60)) % 60;
				int hours = (milliseconds / (1000 * 60 * 60)) % 24;
				
				if (hours > 0) {
						return String.format("%02d:%02d:%02d", hours, minutes, seconds);
				} else {
						return String.format("%02d:%02d", minutes, seconds);
				}
		}
		
		@Override
		protected void onDestroy() {
				super.onDestroy();
				if (waveView != null) {
						waveView.release();
				}
		}
		
		public static class FileUtil {
				public static String convertUriToFilePath(Context context, Uri uri) {
						if (uri == null) {
								return null;
						}
						
						if ("content".equalsIgnoreCase(uri.getScheme())) {
								String[] projection = {MediaStore.Audio.Media.DATA};
								try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
										if (cursor != null && cursor.moveToFirst()) {
												int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
												return cursor.getString(columnIndex);
										}
								} catch (Exception e) {
										e.printStackTrace();
								}
						} else if ("file".equalsIgnoreCase(uri.getScheme())) {
								return uri.getPath();
						}
						return null;
				}
		}
}