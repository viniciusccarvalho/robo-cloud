package io.igx.cloud.robo

import io.igx.cloud.robo.simulation.CoordinateTranslator
import io.igx.cloud.robo.simulation.Radar
import io.igx.cloud.robo.simulation.WorldConfig
import io.igx.cloud.robo.simulation.normalizeAngle
import org.junit.Test
import org.assertj.core.api.Assertions.*
import org.jbox2d.common.Vec2

/**
 * @author vinicius
 *
 */

class RadarTests {

    @Test
    fun `point is inside radar`() {
        val radar = Radar(Vec2(0.0f, 0.0f), 90.0f)
        assertThat(radar.contains(Vec2(10.0f, 500.0f))).isTrue()
        assertThat(radar.contains(Vec2(120.0f, 500.0f))).isFalse()
    }

    @Test
    fun `radar finds target`() {
        var bearing = 250.0f
        val radar = Radar(Vec2(-112.0f, -310.0f), bearing, Math.sqrt(1024.0*1024+768*768).toFloat())
        radar.points.forEachIndexed{i, v ->
            println("$i (${v.x}, ${v.y})")
        }
        val target = Vec2(-450.0f, 66.0f)
        var ticks = 0
        while(!radar.contains(target)){
            bearing += 1.2f
            bearing = normalizeAngle(bearing).toFloat()
            ticks++
            radar.update(radar.center, bearing)
        }
        println("Found target at ${bearing} after $ticks ticks")
        println("Radar status: ")
        radar.points.forEachIndexed{i, v ->
            println("$i (${v.x}, ${v.y})")
        }
    }

    @Test
    fun `test coordinate translation without scale`() {
        val translator = CoordinateTranslator( WorldConfig(), 1.0f)

        val worldScreenStart = translator.screenToWorld(Vec2(0f, 0f))
        val screenCenter = translator.worldToScreen(Vec2(0f, 0f))
        val screenPoint = translator.worldToScreen(Vec2(0f, -40f))
        val worldCenter = translator.screenToWorld(Vec2(512.0f, 384.0f))

        assertThat(Vec2(-512.0f, 384.0f)).isEqualTo(worldScreenStart)
        assertThat(Vec2(512.0f, 384.0f)).isEqualTo(screenCenter)
        assertThat(Vec2(512.0f, 424.0f)).isEqualTo(screenPoint)
        assertThat(Vec2(0f, 0f)).isEqualTo(worldCenter)
    }

    @Test
    fun `test coordinate translation with scale`() {
        val translator = CoordinateTranslator( WorldConfig(), 10.0f)

        val worldScreenStart = translator.screenToWorld(Vec2(0f, 0f))
        val screenCenter = translator.worldToScreen(Vec2(0f, 0f))
        val screenPoint = translator.worldToScreen(Vec2(0f, -10f))
        val worldCenter = translator.screenToWorld(Vec2(512.0f, 384.0f))

        assertThat(Vec2(-51.2f, 38.4f)).isEqualTo(worldScreenStart)
        assertThat(Vec2(512.0f, 384.0f)).isEqualTo(screenCenter)
        assertThat(Vec2(512.0f, 484.0f)).isEqualTo(screenPoint)
        assertThat(Vec2(0f, 0f)).isEqualTo(worldCenter)
    }




}