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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	
	private static final int PERMISSION_REQUEST_CODE = 1000;
	private static final String[] REQUIRED_PERMISSIONS = {
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.RECORD_AUDIO
	};
	
	private Handler handler = new Handler(Looper.getMainLooper());
	private String file = "";
	private double value = 0;
	private String playbackSpeed = "1.0";
	
	private WavePlayerView waveView;
	private EditText filePathEditText;
	private MaterialTextView currentTimeTextView;
	private SeekBar progressBar;
	private MaterialTextView durationTextView, volumeTxt;
	private SeekBar volumeSeekBar;
	private MaterialTextView speedTextView;
	
	private ActivityResultLauncher<Intent> activityResultLauncher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initialize();
		checkPermissions();
	}
	
	private void checkPermissions() {
		if (!hasPermissions()) {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
		} else {
			initializeLogic();
		}
	}
	
	private boolean hasPermissions() {
		for (String permission : REQUIRED_PERMISSIONS) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && allPermissionsGranted(grantResults)) {
				initializeLogic();
			} else {
				Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private boolean allPermissionsGranted(int[] grantResults) {
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
	
	private void initialize() {
		waveView = findViewById(R.id.waveView);
		filePathEditText = findViewById(R.id.edittext1);
		currentTimeTextView = findViewById(R.id.currentTimeTextView);
		progressBar = findViewById(R.id.progressbar1);
		durationTextView = findViewById(R.id.durationTextView);
		volumeSeekBar = findViewById(R.id.volumeSeekBar);
		speedTextView = findViewById(R.id.speedTextView);
        volumeTxt = findViewById(R.id.volumeTxt);
		
		setupButtons();
		setupSeekBars();
		setupActivityResultLauncher();
	}
	
	private void setupButtons() {
		findViewById(R.id.button2).setOnClickListener(v -> waveView.startListening());
		findViewById(R.id.button1).setOnClickListener(v -> waveView.stopListening());
		findViewById(R.id.pick).setOnClickListener(v -> pickAudioFile());
		findViewById(R.id.play_with_file_path).setOnClickListener(v -> playAudioFromFile());
		findViewById(R.id.play_from_app).setOnClickListener(v -> playAudioFromApp());
		findViewById(R.id.stop).setOnClickListener(v -> stopAudio());
		findViewById(R.id.resume).setOnClickListener(v -> resumeAudio());
		findViewById(R.id.pause).setOnClickListener(v -> waveView.pauseAudioWithWave());
		findViewById(R.id.button4).setOnClickListener(v -> setPlaybackSpeed());
		findViewById(R.id.update).setOnClickListener(v -> updateWaveProperties());
		findViewById(R.id.hide_wave2).setOnClickListener(v -> waveView.setWave2Visible(false));
		findViewById(R.id.show_wave2).setOnClickListener(v -> waveView.setWave2Visible(true));
	}
	
	private void setupSeekBars() {
		progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				value = progress;
				updateCurrentTimeText(progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				waveView.pauseAudioWithWave();
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				waveView.seekToPosition((int) value);
				waveView.resumeAudioWithWave();
			}
		});
		
		volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float volume = progress / 100f;
				waveView.setVolume(volume, volume);
                volumeTxt.setText(String.valueOf(volume));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}
	
	private void setupActivityResultLauncher() {
		activityResultLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
				handleFilePickResult(result.getData());
			}
		}
		);
	}
	
	private void initializeLogic() {
		waveView.setOnAudioCompleteListener(() -> {
			progressBar.setProgress(0);
			updateCurrentTimeText(0);
		});
		playbackSpeed = "1.0";
		speedTextView.setText("1.0x");
		volumeSeekBar.setProgress(100);
	}
	
	private void pickAudioFile() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.setType("audio/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		activityResultLauncher.launch(intent);
	}
	
	private void handleFilePickResult(Intent data) {
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
	
	private void playAudioFromFile() {
		file = filePathEditText.getText().toString().trim();
		if (!file.isEmpty()) {
			setupAudioPlayer();
			waveView.playAudioWithWaveFromPath(file);
			updatePlaybackSpeed();
		} else {
			Toast.makeText(this, "Please select an audio file", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void playAudioFromApp() {
		setupAudioPlayer();
		waveView.playAudioWithWave(R.raw.mus);
		updatePlaybackSpeed();
	}
	
	private void setupAudioPlayer() {
		waveView.setOnAudioProgressListener(this::updateProgress);
		handler.postDelayed(this::updateDuration, 50);
	}
	
	private void updateProgress(int currentTime) {
		updateCurrentTimeText(currentTime);
		progressBar.setProgress(currentTime);
	}
	
	private void updateDuration() {
		int duration = waveView.getAudioDuration();
		durationTextView.setText(formatMilliseconds(duration));
		progressBar.setMax(duration);
	}
	
	private void stopAudio() {
		waveView.stopAudioWithWave();
		progressBar.setProgress(0);
		updateCurrentTimeText(0);
	}
	
	private void resumeAudio() {
		waveView.resumeAudioWithWave();
		updatePlaybackSpeed();
	}
	
	private void setPlaybackSpeed() {
		playbackSpeed = ((EditText) findViewById(R.id.edittext7)).getText().toString();
		updatePlaybackSpeed();
	}
	
	private void updatePlaybackSpeed() {
		try {
			float speed = Float.parseFloat(playbackSpeed);
			waveView.setPlaybackSpeed(playbackSpeed);
			speedTextView.setText(String.format("%.1fx", speed));
		} catch (NumberFormatException e) {
			Toast.makeText(this, "Invalid speed format", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void updateWaveProperties() {
		try {
			int color1 = Color.parseColor(((EditText) findViewById(R.id.edittext2)).getText().toString());
			float wave1Width = Float.parseFloat(((EditText) findViewById(R.id.edittext3)).getText().toString());
			int color2 = Color.parseColor(((EditText) findViewById(R.id.edittext4)).getText().toString());
			float wave2Width = Float.parseFloat(((EditText) findViewById(R.id.edittext5)).getText().toString());
			float waveSpeed = Float.parseFloat(((EditText) findViewById(R.id.edittext6)).getText().toString());
			
			waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "Invalid wave properties", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void updateCurrentTimeText(int milliseconds) {
		currentTimeTextView.setText(formatMilliseconds(milliseconds));
	}
	
	private String formatMilliseconds(int milliseconds) {
		int seconds = (milliseconds / 1000) % 60;
		int minutes = (milliseconds / (1000 * 60)) % 60;
		int hours = (milliseconds / (1000 * 60 * 60)) % 24;
		
		return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
		: String.format("%02d:%02d", minutes, seconds);
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
			if (uri == null) return null;
			
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