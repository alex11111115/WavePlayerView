package sound.wave.kilobyte;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // السياق الخاص بالتطبيق.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("sound.wave.kilobyte.test", appContext.getPackageName());
    }

    @Test
    public void testPlayAudioWithWave() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        WavePlayerView wavePlayerView = new WavePlayerView(appContext, null);

        // نتأكد من أنه يمكن إعداد مشغل الصوت وتشغيل الصوت
        wavePlayerView.playAudioWithWave(R.raw.sample_audio);
        assertNotNull(wavePlayerView.getMediaPlayer());
        assertTrue(wavePlayerView.getMediaPlayer().isPlaying());
    }
}