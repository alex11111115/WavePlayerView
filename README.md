
[![StandWithPalestine](https://raw.githubusercontent.com/karim-eg/StandWithPalestine/main/assets/palestine_badge.svg)](https://github.com/karim-eg/StandWithPalestine)   [![](https://jitpack.io/v/alex11111115/WavePlayerView.svg)](https://jitpack.io/#alex11111115/WavePlayerView) 

[![ReadMeSupportPalestine](https://raw.githubusercontent.com/Safouene1/support-palestine-banner/master/banner-support.svg)](https://techforpalestine.org/learn-more)


# WavePlayerView ![http://developer.android.com/index.html](https://github.com/alex11111115/WavePlayerView/assets/96258291/3fc4547a-a929-43c3-8cf0-ee16b066fb7e)

<a href="https://github.com/alex11111115/WavePlayerView">
  <img src="https://img.shields.io/badge/WavePlayerView-8E8DE5?style=flat&labelColor=E8D8FB&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjQwIiB2aWV3Qm94PSIwIDAgMTAwIDQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxwb2x5bGluZSBwb2ludHM9IjAsMjAgMTAsMTAgMjAsMjAgMzAsMTAgNDAsMjAgNTAsMTAgNjAsMjAgNzAsMTAgODAsMjAgOTAsMTAgMTAwLDIwIiBzdHlsZT0iZmlsbDpub25lO3N0cm9rZTojOEU4REU1O3N0cm9rZS13aWR0aDoyIiAvPjxwb2x5bGluZSBwb2ludHM9IjAsMjUgMTAsMTUgMjAsMjUgMzAsMTUgNDAsMjUgNTAsMTUgNjAsMjUgNzAsMTUgODAsMjUgOTAsMTUgMTAwLDI1IiBzdHlsZT0iZmlsbDpub25lO3N0cm9rZTojOEU4REU1O3N0cm9rZS13aWR0aDoyIiAvPjwvc3ZnPg==" alt="WavePlayerView Logo" style="width: 180px; height: 28px; margin-right: 10px;">
  <span style="font-size: 12px; color: #8E8DE5;"></span>
</a>

WavePlayerView is an advanced library for real-time sound analysis by recording sound and instantly plotting waves based on frequencies and visualizing waves from an audio file or a track within your application.

## Features

- **Real-time Sound Analysis**: Display sound waves in real-time while recording or playing an audio file.
- **Customizable Display**: Easily adjust wave colors, stroke widths, and wave speed.
- **User-friendly**: Simple and intuitive API for seamless integration.
- **Wide Compatibility**: Supports Android API level 21 and above. [![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21) 
- **Advanced Controls**: Control playback speed, pause/resume functionality, and monitor audio progress.

## Screenshot

![Screenshot_٢٠٢٤٠٦٠٥-١٠٢٤٥٠_Sound Wave Visualization](https://github.com/alex11111115/WavePlayerView/assets/96258291/5b185180-3fcf-4078-ba9c-75c9aa9c776f) 
![Screenshot_٢٠٢٤٠٦٠٥-١٠٢٤١٨_Sound Wave Visualization](https://github.com/alex11111115/WavePlayerView/assets/96258291/b356224d-e6d7-4fd0-8a4b-16d778f859fc)
![Screenshot_٢٠٢٤٠٦٠٥-١٤٥١٥٨_Wave Player](https://github.com/alex11111115/WavePlayerView/assets/96258291/beaf3a5b-49b9-4487-b3f9-94cbeb1cb0d3) 
![Screenshot_٢٠٢٤٠٦٠٥-١٤٥٢٠٢_Wave Player](https://github.com/alex11111115/WavePlayerView/assets/96258291/3dd56ed2-d9d2-4baf-a337-b64df2c9e309)
![Screenshot_٢٠٢٤٠٦٠٥-١٤٥٢٠٥_Wave Player](https://github.com/alex11111115/WavePlayerView/assets/96258291/16ad97cd-128e-4483-93d7-fd80ab55246a)

![Screenshot_٢٠٢٤٠٦٠٧-٠٩٢٣٠٢_WavePlayerView](https://github.com/alex11111115/WavePlayerView/assets/96258291/a6b7698b-751b-47ce-961d-33314c1c0795)

## Why Use WavePlayerView?  ![68747470733a2f2f6170692e636f646163792e636f6d2f70726f6a6563742f62616467652f47726164652f3136353766333032633330663437393738653762383233643164323863363439](https://github.com/alex11111115/WavePlayerView/assets/96258291/bdc69ed8-47b0-42a5-a418-802819291ec7)


- **High Performance**: Engineered for high efficiency with minimal impact on your application's performance.
- **Open Source**: Full access to source code for customization and enhancement.
- **Community Support**: Supported by a dedicated community, with opportunities for contributions and improvements.

## How to Use the Library? ![GitHub top language](https://img.shields.io/github/languages/top/alex11111115/WavePlayerView?style=flat&color=red)

### 1. Add it in your root build.gradle at the end of repositories:

```gradle
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
            //...
			maven { url 'https://jitpack.io' }
		}
	}
```

### 2. Add the Library to Your Project

> Add the following to your project's `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.alex11111115:WavePlayerView:1.10'
}
```

### 3. Use WavePlayerView in Your Layout

> Add `WavePlayerView` to your activity layout file `activity_main.xml`:

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

### 4. Control the Waves from Your Code

> In your `MainActivity.java` file, you can control the `WavePlayerView` as follows:

> To play from file path:
```java
// Play audio from a file path
waveView.playAudioWithWaveFromPath(filePath);
```

> To play from your app resources:
```java
// Play audio from app resources
waveView.playAudioWithWave(R.raw.mus); //Replace 'mus' with your audio
```

> To pause media and waves:
```java
// Pause media and waves
waveView.pauseAudioWithWave();
```

> To resume media and waves:
```java
// Resume media and waves
waveView.resumeAudioWithWave();
```

> To stop media and waves:
```java
// Stop media and waves
waveView.stopAudioWithWave();
```

> To start listening and show waves live:
```java
// Start listening and show live waves
waveView.startListening();
```

> To stop listening:
```java
// Stop listening
waveView.stopListening();
```

> To set playback speed:
```java
// Set playback speed (default is "1.0")
waveView.setPlaybackSpeed("1.0");
```

> To set wave properties:
```java
// Set wave properties
waveView.setWaveProperties(color1, wave1Width, color2, wave2Width, waveSpeed);
```

> To control the visibility of the second wave:
```java
// Hide the second wave
waveView.setWave2Visible(false);

// Show the second wave
waveView.setWave2Visible(true);
```

> To handle audio completion events:
```java
// Handle audio completion events
waveView.setOnAudioCompleteListener(() -> progressbar1.setProgress(0));
```

> To track audio progress:
```java
// Track audio progress
waveView.setOnAudioProgressListener(currentMilliSec -> {
    //your codes here
});
```

> To seek to a position in the media:
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

> Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

> Put this in "onDestroy" as follows
```java
if (waveView != null) {
  waveView.release();
}
```

## Credits

WavePlayerView is developed and maintained by [alex11111115](https://github.com/alex11111115).

## Contribution ![GitHub contributors](https://img.shields.io/github/contributors/alex11111115/WavePlayerView?color=blue&style=flat-square)

We welcome contributions from the community! If you have ideas or improvements, feel free to submit pull requests or open issues on the [GitHub repository](https://github.com/alex11111115/WavePlayerView). 

Thanks to these amazing people for their contributions

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind are welcome!

## License

WavePlayerView is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt). ![GitHub license](https://img.shields.io/github/license/alex11111115/WavePlayerView?style=flat&color=blue)

[![StandWithPalestine](https://raw.githubusercontent.com/Safouene1/support-palestine-banner/master/StandWithPalestine.svg)](https://techforpalestine.org/learn-more) [![StandWithPalestineBadgeBordered](https://raw.githubusercontent.com/saedyousef/StandWithPalestine/main/badges/flat/bordered/StandWithPalestine.svg)](https://techforpalestine.org/learn-more)

## More Info

<div style="display: flex; gap: 10px; flex-wrap: wrap;">
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Repo Size</p>
    <img src="https://img.shields.io/github/repo-size/alex11111115/WavePlayerView?style=flat&color=blue" alt="Repo Size">
  </div>
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Code Size</p>
    <img src="https://img.shields.io/github/languages/code-size/alex11111115/WavePlayerView?style=flat&color=orange" alt="Code Size">
  </div>
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Last Commit</p>
    <img src="https://img.shields.io/github/last-commit/alex11111115/WavePlayerView?style=flat&color=yellow" alt="Last Commit">
  </div>
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Latest Release</p>
    <img src="https://img.shields.io/github/v/release/alex11111115/WavePlayerView?style=flat&color=blue" alt="Latest Release">
  </div>
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Project Status</p>
    <img src="https://img.shields.io/badge/status-active-brightgreen?style=flat" alt="Project Status">
  </div>
  <div style="border: 1px solid #e1e4e8; border-radius: 5px; padding: 10px; width: 200px; text-align: center;">
    <p>Build Status</p>
    <img src="https://img.shields.io/github/actions/workflow/status/alex11111115/WavePlayerView/ci.yml?style=flat&color=brightgreen" alt="Build Status">
  </div>
</div>
