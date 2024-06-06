package sound.wave.kilobyte;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;




public class MainActivity extends AppCompatActivity {
	
	public final int REQ_CD_W = 101;
	
	private Handler handler = new Handler();
	private Context context = this;
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private String file = "";
	private double value = 0;
	private String s = "";
	
	private WavePlayerView waveView;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private LinearLayout linear2;
	private LinearLayout linear9;
	private LinearLayout linear7;
	private LinearLayout linear8;
	private Button button2;
	private Button button3;
	private Button pick;
	private EditText edittext1;
	private Button play_with_file_path;
	private Button play_from_app;
	private Button stop;
	private Button resume;
	private Button pause;
	private EditText edittext7;
	private Button button4;
	private LinearLayout linear5;
	private LinearLayout linear6;
	private EditText edittext2;
	private EditText edittext3;
	private EditText edittext4;
	private EditText edittext5;
	private EditText edittext6;
	private Button update;
	private TextView currentTimeTextView;
	private SeekBar progressbar1;
	private TextView durationTextView;
	private Button button1;
	private Button hide_wave2;
	private Button show_wave2;
	
	private Intent w = new Intent(Intent.ACTION_GET_CONTENT);
	private Intent i = new Intent();
	
	private void stopAudio() {
				if (waveView != null) {
							waveView.stopAudioWithWave();
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
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		waveView = findViewById(R.id.waveView);
		linear3 = findViewById(R.id.linear3);
		linear4 = findViewById(R.id.linear4);
		linear2 = findViewById(R.id.linear2);
		linear9 = findViewById(R.id.linear9);
		linear7 = findViewById(R.id.linear7);
		linear8 = findViewById(R.id.linear8);
		button2 = findViewById(R.id.button2);
		button3 = findViewById(R.id.button3);
		pick = findViewById(R.id.pick);
		edittext1 = findViewById(R.id.edittext1);
		play_with_file_path = findViewById(R.id.play_with_file_path);
		play_from_app = findViewById(R.id.play_from_app);
		stop = findViewById(R.id.stop);
		resume = findViewById(R.id.resume);
		pause = findViewById(R.id.pause);
		edittext7 = findViewById(R.id.edittext7);
		button4 = findViewById(R.id.button4);
		linear5 = findViewById(R.id.linear5);
		linear6 = findViewById(R.id.linear6);
		edittext2 = findViewById(R.id.edittext2);
		edittext3 = findViewById(R.id.edittext3);
		edittext4 = findViewById(R.id.edittext4);
		edittext5 = findViewById(R.id.edittext5);
		edittext6 = findViewById(R.id.edittext6);
		update = findViewById(R.id.update);
		currentTimeTextView = findViewById(R.id.currentTimeTextView);
		progressbar1 = findViewById(R.id.progressbar1);
		durationTextView = findViewById(R.id.durationTextView);
		button1 = findViewById(R.id.button1);
		hide_wave2 = findViewById(R.id.hide_wave2);
		show_wave2 = findViewById(R.id.show_wave2);
		w.setType("audio/*");
		w.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.startListening();
			}
		});
		
		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.stopListening();
			}
		});
		
		pick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivityForResult(w, REQ_CD_W);
			}
		});
		
		play_with_file_path.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {
							// Permission is not granted
							ActivityCompat.requestPermissions(MainActivity.this,
							new String[]{Manifest.permission.RECORD_AUDIO},
							9653);
				} else {
							
					waveView.setOnAudioProgressListener(currentTime -> {
								String currentTimeStr = formatMilliseconds(currentTime);
								currentTimeTextView.setText(currentTimeStr);
						progressbar1.setProgress((int)currentTime);
					});
					
					file = edittext1.getText().toString();
					waveView.playAudioWithWaveFromPath(file);
					handler.postDelayed(() -> {
								int duration = waveView.getAudioDuration();
								String durationStr = formatMilliseconds(duration);
								durationTextView.setText(durationStr);
						progressbar1.setMax((int)duration);
					}, 100);
					
				}
				waveView.setPlaybackSpeed(s);
				
			}
		});
		
		play_from_app.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {
							// Permission is not granted
							ActivityCompat.requestPermissions(MainActivity.this,
							new String[]{Manifest.permission.RECORD_AUDIO},
							9653);
				} else {
							
					waveView.setOnAudioProgressListener(currentTime -> {
								String currentTimeStr = formatMilliseconds(currentTime);
								currentTimeTextView.setText(currentTimeStr);
						progressbar1.setProgress((int)currentTime);
					});
					
					waveView.playAudioWithWave(R.raw.mus);
					handler.postDelayed(() -> {
								int duration = waveView.getAudioDuration();
								String durationStr = formatMilliseconds(duration);
								durationTextView.setText(durationStr);
						progressbar1.setMax((int)duration);
					}, 1000);
					
				}
				waveView.setPlaybackSpeed(s);
				
			}
		});
		
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				stopAudio();
			}
		});
		
		resume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.resumeAudioWithWave();
				waveView.setPlaybackSpeed(s);
				
			}
		});
		
		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.pauseAudioWithWave();
			}
		});
		
		button4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				s = edittext7.getText().toString();
				waveView.setPlaybackSpeed(s);
				
			}
		});
		
		update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				String editText2Value = edittext2.getText().toString();
				String editText3Value = edittext3.getText().toString();
				String editText4Value = edittext4.getText().toString();
				String editText5Value = edittext5.getText().toString();
				String editText6Value = edittext6.getText().toString();
				
				if (!(editText2Value.isEmpty() && editText3Value.isEmpty() && editText4Value.isEmpty() && editText5Value.isEmpty() && editText6Value.isEmpty())) {
							try {
										int color1 = Color.parseColor(editText2Value);
										float wave1Width = Float.parseFloat(editText3Value);
										int color2 = Color.parseColor(editText4Value);
										float wave2Width = Float.parseFloat(editText5Value);
										float waveSpeed = Float.parseFloat(editText6Value);
										
										waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
							} catch (IllegalArgumentException e) {
										// Handle invalid input format or color string
										e.printStackTrace();
							}
				}
				
			}
		});
		
		progressbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar _param1, int _param2, boolean _param3) {
				final int _progressValue = _param2;
				value = _progressValue;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar _param1) {
				waveView.pauseAudioWithWave();
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar _param2) {
				waveView.seekToPosition((int) value);
				waveView.setPlaybackSpeed(s);
				
			}
		});
		
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				i.setClass(getApplicationContext(), AudioRecordActivity.class);
				startActivity(i);
			}
		});
		
		hide_wave2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.setWave2Visible(false);
			}
		});
		
		show_wave2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				waveView.setWave2Visible(true);
			}
		});
	}
	
	private void initializeLogic() {
		
		
		
		waveView.setOnAudioCompleteListener(new WavePlayerView.OnAudioCompleteListener() {
					@Override
					public void onComplete() {
								
								progressbar1.setProgress((int)0);
								
					}
		});
		
		s = "1.0";
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			case REQ_CD_W:
			if (_resultCode == Activity.RESULT_OK) {
				ArrayList<String> _filePath = new ArrayList<>();
				if (_data != null) {
					if (_data.getClipData() != null) {
						for (int _index = 0; _index < _data.getClipData().getItemCount(); _index++) {
							ClipData.Item _item = _data.getClipData().getItemAt(_index);
							_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _item.getUri()));
						}
					}
					else {
						_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _data.getData()));
					}
				}
				edittext1.setText(_filePath.get((int)(0)));
			}
			else {
				
			}
			break;
			default:
			break;
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (waveView != null) {
					waveView.release();
		}
		
	}
}
