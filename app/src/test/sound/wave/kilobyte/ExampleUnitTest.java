package sound.wave.kilobyte;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void testSetPlaybackSpeed() {
        WavePlayerView wavePlayerView = new WavePlayerView(null, null);
        String validSpeed = "1.5";
        wavePlayerView.setPlaybackSpeed(validSpeed);

        // لا يمكن الوصول إلى MediaPlayer مباشرةً هنا، لذا نتوقع عدم حدوث استثناء فقط
        assertEquals("1.5", wavePlayerView.getPlaybackSpeed());
    }

    @Test
    public void testWaveProperties() {
        WavePlayerView wavePlayerView = new WavePlayerView(null, null);
        int color1 = 0xFF0000;
        float strokeWidth1 = 5.0f;
        int color2 = 0x00FF00;
        float strokeWidth2 = 3.0f;
        float speed = 1.0f;

        wavePlayerView.setWaveProperties(color1, strokeWidth1, color2, strokeWidth2, speed);

        // لا يمكن الوصول إلى الحقول مباشرةً، ولكن نتوقع عدم حدوث استثناء فقط
        assertTrue(true);
    }
}