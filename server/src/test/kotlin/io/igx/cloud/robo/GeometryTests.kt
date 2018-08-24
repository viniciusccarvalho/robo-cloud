package io.igx.cloud.robo

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.junit.Test
import org.assertj.core.api.Assertions.*

/**
 * @author vinicius
 *
 */

class RadarTests {

    @Test
    fun `point is inside radar`() {
        val radar = Radar(Vector2D(0.0, 0.0), 90.0)
        assertThat(radar.contains(Vector2D(10.0, 500.0))).isTrue()
        assertThat(radar.contains(Vector2D(120.0, 500.0))).isFalse()
    }


}