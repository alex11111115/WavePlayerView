```markdown
# WavePlayerView

WavePlayerView is an advanced library for real-time sound analysis by recording sound and instantly plotting waves based on frequencies. But that's not all! You can also visualize waves from an audio file or a track within your application.

## Features

- **Real-time Sound Analysis**: Display sound waves in real-time while recording or playing an audio file.
- **Customizable Display**: Easily adjust wave colors, stroke widths, and wave speed.
- **User-friendly**: Simple and intuitive API for seamless integration.
- **Wide Compatibility**: Supports Android API level 21 and above.
- **Advanced Controls**: Control playback speed, pause/resume functionality, and monitor audio progress.

## Why Use WavePlayerView?

- **High Performance**: Engineered for high efficiency with minimal impact on your application's performance.
- **Open Source**: Full access to source code for customization and enhancement.
- **Community Support**: Supported by a dedicated community, with opportunities for contributions and improvements.

## How to Use the Library?

### 1. Add the Library to Your Project

Add the following to your project's `build.gradle` file:

```gradle
dependencies {
    implementation 'sound.wave.kilobyte:WavePlayerView:1.0'
}
```

### 2. Use WavePlayerView in Your Layout

Add `WavePlayerView` to your activity layout file `activity_main.xml`:

```xml
<sound.wave.kilobyte.WavePlayerView
    android:id="@+id/waveView"
    android:layout_width="250dp"
    android:layout_height="75dp"
    android:padding="2dp"
    android:gravity="center"
    android:orientation="horizontal"
    android:layout_gravity="center"
    app:waveColor1="#FF8E8DE5"
    app:waveStrokeWidth1="5dp"
    app:waveColor2="#FFE8D8FB"
    app:waveStrokeWidth2="5dp"
    app:waveSpeed="0.5"
    app:wave2Visible="true" />
```

### 3. Control the Waves from Your Code

In your `MainActivity.java` file, you can control the `WavePlayerView` as follows:

//Initialization first
```java
private WavePlayerView waveView;
waveView = findViewById(R.id.waveView);
```

To play from file path:
```java
// Play audio from a file path
waveView.playAudioWithWaveFromPath(filePath);
```

To play from your app resources:
```java
// Play audio from app resources
waveView.playAudioWithWave(R.raw.mus);
```

To pause media and waves:
```java
// Pause media and waves
waveView.pauseAudioWithWave();
```

To resume media and waves:
```java
// Resume media and waves
waveView.resumeAudioWithWave();
```

To stop media and waves:
```java
// Stop media and waves
waveView.stopAudioWithWave();
```

To start listening and show waves live:
```java
// Start listening and show live waves
waveView.startListening();
```

To stop listening:
```java
// Stop listening
waveView.stopListening();
```

To set playback speed:
```java
// Set playback speed (default is "1.0")
waveView.setPlaybackSpeed("1.0");
```

To set wave properties:
```java
// Set wave properties
waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
```

To control the visibility of the second wave:
```java
// Hide the second wave
waveView.setWave2Visible(false);

// Show the second wave
waveView.setWave2Visible(true);
```

To handle audio completion events:
```java
// Handle audio completion events
waveView.setOnAudioCompleteListener(() -> progressbar1.setProgress(0));
```

To track audio progress:
```java
// Track audio progress
waveView.setOnAudioProgressListener(currentTime -> {
    String currentTimeStr = formatMilliseconds(currentTime);
    currentTimeTextView.setText(currentTimeStr);
    progressbar1.setProgress((int) currentTime);
});

file = edittext1.getText().toString();
waveView.playAudioWithWaveFromPath(file);

handler.postDelayed(() -> {
    int duration = waveView.getAudioDuration();
    String durationStr = formatMilliseconds(duration);
    durationTextView.setText(durationStr);
    progressbar1.setMax((int) duration);
}, 50);

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
```

To seek to a position in the media:
```java
// Seek to a position in the media
progressbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value = progress; // Store value
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        waveView.pauseAudioWithWave(); // Pause media
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        waveView.seekToPosition((int) value); // Seek to new position
        waveView.setPlaybackSpeed(s);
    }
});
```

### Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.RECORD_BACKGROUND_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

Request permission to record audio at runtime:

```java
if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
    // Permission not granted
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9653);
} else {
    // Permission granted
}
```

//Don't forget to put this in "onDestroy" as follows
```java
if (waveView != null) {
  waveView.release();
}
```

## Credits

WavePlayerView is developed and maintained by [alex11111115](https://github.com/alex11111115).

## Contribution

We welcome contributions from the community! If you have ideas or improvements, feel free to submit pull requests or open issues on the [GitHub repository](https://github.com/alex11111115/WavePlayerView).

## License

WavePlayerView is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).
```

### Explanation of Sections:
- **Introduction**: Provides a brief description of the library and its standout features.
- **Features**: Lists the main features of the library.
- **Why Use WavePlayerView?**: Highlights the reasons to choose this library.
- **How to Use the Library?**: Step-by-step guide on adding the library to a project and using it.
- **Permissions**: Details the necessary permissions and how to request them.
- **Credits**: Acknowledges the developer or maintainers of the library.
- **Contribution**: Encourages community involvement and contributions.
- **License**: Specifies the license under which the library is distributed.