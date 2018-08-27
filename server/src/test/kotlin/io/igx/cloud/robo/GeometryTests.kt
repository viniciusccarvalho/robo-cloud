package io.igx.cloud.robo

import io.igx.cloud.robo.simulation.Radar
import io.igx.cloud.robo.simulation.normalizeAngle
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.junit.Test
import org.assertj.core.api.Assertions.*
import java.util.*

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

    @Test
    fun `radar finds target`() {
        var bearing = 250.0
        val radar = Radar(Vector2D(-112.0, -310.0), bearing, Math.sqrt(1024.0*1024+768*768))
        radar.points.forEachIndexed{i, v ->
            println("$i (${v.x}, ${v.y})")
        }
        val target = Vector2D(-450.0, 66.0)
        var ticks = 0
        while(!radar.contains(target)){
            bearing += 1.2
            bearing = normalizeAngle(bearing)
            ticks++
            radar.update(radar.center, bearing)
        }
        println("Found target at ${bearing} after $ticks ticks")
        println("Radar status: ")
        radar.points.forEachIndexed{i, v ->
            println("$i (${v.x}, ${v.y})")
        }
    }




}