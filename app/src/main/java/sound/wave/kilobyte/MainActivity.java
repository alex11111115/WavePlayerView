package sound.wave.kilobyte;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String audioFilePath = "";
    private double seekPosition = 0;
    private float playbackSpeed = 1.0f;

    private WavePlayerView waveView;
    private EditText filePathEditText, wave1AlphaEditText, wave2AlphaEditText;
    private MaterialTextView currentTimeTextView, durationTextView, volumeTextView, speedTextView;
    private SeekBar progressBar, volumeSeekBar;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeViews();
        setupLaunchers();
        checkPermissions();
        setupPermissionLauncher();
    }

    private void initializeViews() {
        waveView = findViewById(R.id.waveView);
        filePathEditText = findViewById(R.id.edittext1);
        wave1AlphaEditText = findViewById(R.id.edittext8);
        wave2AlphaEditText = findViewById(R.id.edittext9);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        progressBar = findViewById(R.id.progressbar1);
        durationTextView = findViewById(R.id.durationTextView);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        speedTextView = findViewById(R.id.speedTextView);
        volumeTextView = findViewById(R.id.volumeTxt);

        setupButtons();
        setupSeekBars();
    }
    
    private void setupLaunchers() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleFilePickResult(result.getData().getData());
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }
                }
        );
    }

    private void checkPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            initializeLogic();
        }
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // تم منح جميع الصلاحيات
                    Toast.makeText(this, "تم منح جميع الصلاحيات المطلوبة", Toast.LENGTH_SHORT).show();
                } else {
                    // لم يتم منح جميع الصلاحيات
                    Toast.makeText(this, "لم يتم منح بعض الصلاحيات المطلوبة", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private boolean checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false;
        }

        return true;
    }
    
    @Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_EXTERNAL_STORAGE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // تم منح الصلاحية، يمكنك الآن تنفيذ العملية المطلوبة
            Toast.makeText(this, "تم منح صلاحية الوصول إلى الملفات", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "تم رفض صلاحية الوصول إلى الملفات", Toast.LENGTH_SHORT).show();
        }
    }
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
        findViewById(R.id.setAlpha1).setOnClickListener(v -> setWaveAlpha(wave1AlphaEditText, true));
        findViewById(R.id.setAlpha2).setOnClickListener(v -> setWaveAlpha(wave2AlphaEditText, false));
    }

    private void setupSeekBars() {
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekPosition = progress;
                updateCurrentTimeText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                waveView.pauseAudioWithWave();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                waveView.seekToPosition((int) seekPosition);
                waveView.resumeAudioWithWave();
                setupAudioPlayer();
                updatePlaybackSpeed();
            }
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                waveView.setVolume(volume, volume);
                volumeTextView.setText(String.format(Locale.getDefault(), "%.2f", volume));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initializeLogic() {
        waveView.setOnAudioCompleteListener(() -> {
            progressBar.setProgress(0);
            updateCurrentTimeText(0);
        });
        speedTextView.setText("1.0x");
        volumeSeekBar.setProgress(100);
    }

    private void pickAudioFile() {
        if (checkAndRequestPermissions()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            filePickerLauncher.launch(intent);
        }
    }

    private void handleFilePickResult(Uri uri) {
        if (uri != null) {
            try {
                audioFilePath = FileUtil.getAudioPathFromUri(this, uri);
                if (audioFilePath != null) {
                    filePathEditText.setText(audioFilePath);
                } else {
                    throw new IOException("فشل في الحصول على مسار الملف");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling file pick result", e);
                Toast.makeText(this, "حدث خطأ أثناء اختيار الملف", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playAudioFromFile() {
        if (checkAndRequestPermissions()) {
            audioFilePath = filePathEditText.getText().toString().trim();
            if (!audioFilePath.isEmpty()) {
                try {
                    setupAudioPlayer();
                    waveView.playAudioWithWaveFromPath(audioFilePath);
                    updatePlaybackSpeed();
                } catch (Exception e) {
                    Log.e(TAG, "Error playing audio from file", e);
                    Toast.makeText(this, "فشل في تشغيل الملف الصوتي", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "الرجاء اختيار ملف صوتي", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playAudioFromApp() {
        try {
            setupAudioPlayer();
            waveView.playAudioWithWave(R.raw.mus);
            updatePlaybackSpeed();
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio from app", e);
            Toast.makeText(this, "فشل في تشغيل الصوت من التطبيق", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAudioPlayer() {
        waveView.setOnAudioProgressListener(this::updateProgress);
        handler.postDelayed(this::updateDuration, 50);
    }

    private void updateProgress(int currentTime) {
        runOnUiThread(() -> {
            updateCurrentTimeText(currentTime);
            progressBar.setProgress(currentTime);
        });
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
        setupAudioPlayer();
        updatePlaybackSpeed();
    }

    private void setPlaybackSpeed() {
        try {
            playbackSpeed = Float.parseFloat(((EditText) findViewById(R.id.edittext7)).getText().toString());
            updatePlaybackSpeed();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "صيغة السرعة غير صالحة", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlaybackSpeed() {
        try {
            waveView.setPlaybackSpeed(String.valueOf(playbackSpeed));
            speedTextView.setText(String.format(Locale.getDefault(), "%.1fx", playbackSpeed));
        } catch (Exception e) {
            Log.e(TAG, "Error updating playback speed", e);
            Toast.makeText(this, "فشل في تحديث سرعة التشغيل", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWaveProperties() {
        try {
            int color1 = parseColor(((EditText) findViewById(R.id.edittext2)).getText().toString());
            float wave1Width = parseFloat(((EditText) findViewById(R.id.edittext3)).getText().toString());
            int color2 = parseColor(((EditText) findViewById(R.id.edittext4)).getText().toString());
            float wave2Width = parseFloat(((EditText) findViewById(R.id.edittext5)).getText().toString());
            float waveSpeed = parseFloat(((EditText) findViewById(R.id.edittext6)).getText().toString());
            
            waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
        } catch (Exception e) {
            Log.e(TAG, "Error updating wave properties", e);
            Toast.makeText(this, "خصائص الموجة غير صالحة", Toast.LENGTH_SHORT).show();
        }
    }

    private int parseColor(String colorString) throws IllegalArgumentException {
        return Color.parseColor(colorString);
    }

    private float parseFloat(String floatString) throws NumberFormatException {
        return Float.parseFloat(floatString);
    }

    private void setWaveAlpha(EditText editText, boolean isWave1) {
        try {
            float alpha = Float.parseFloat(editText.getText().toString());
            if (isWave1) {
                waveView.setWave1Alpha(alpha);
            } else {
                waveView.setWave2Alpha(alpha);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error setting wave alpha", e);
            Toast.makeText(this, "الرجاء إدخال قيمة صحيحة للشفافية", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentTimeText(int milliseconds) {
        currentTimeTextView.setText(formatMilliseconds(milliseconds));
    }
    
    private boolean checkStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            return false;
        }
    }
    return true;
}

    private String formatMilliseconds(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = (milliseconds / (1000 * 60 * 60)) % 24;

        return hours > 0 ? String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waveView != null) {
            waveView.release();
        }
        handler.removeCallbacksAndMessages(null);
    }

  public static class FileUtil {
    private static final String TAG = "FileUtil";

    public static String getAudioPathFromUri(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    // Handle different types of document URIs
                    switch (getDocumentType(uri)) {
                        case "primary":
                            return Environment.getExternalStorageDirectory() + "/" + getDocumentId(uri).split(":")[1];
                        case "downloads":
                            return getDownloadsDocumentPath(context, uri);
                        case "audio":
                            return getMediaDocumentPath(context, uri, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        default:
                            Log.w(TAG, "Unsupported document type: " + getDocumentType(uri));
                            break;
                    }
                }
            }

            // Handle other types of URIs
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getAudioDataColumn(context, uri);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            } else {
                Log.w(TAG, "Unsupported URI scheme: " + uri.getScheme());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting path from URI: " + uri, e);
        }

        return null;
    }

    private static String getAudioDataColumn(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting data column", e);
        }
        return null;
    }

    private static String getDocumentType(Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        return docId.split(":")[0];
    }

    private static String getDocumentId(Uri uri) {
        return DocumentsContract.getDocumentId(uri);
    }

    private static String getDownloadsDocumentPath(Context context, Uri uri) {
        final String id = getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
        return getAudioDataColumn(context, contentUri);
    }

    private static String getMediaDocumentPath(Context context, Uri uri, Uri contentUri) {
        final String docId = getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            Log.w(TAG, "Unsupported media type: " + type);
            return null;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{split[1]};
        return getAudioDataColumn(context, ContentUris.withAppendedId(contentUri, Long.parseLong(split[1])));
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
  }
}