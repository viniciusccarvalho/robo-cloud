package io.igx.cloud.robo.simulation

import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2


data class Dimension(val width: Int, val height: Int)
data class WorldConfig(val screen: Dimension = Dimension(1024, 768), val botBox : Dimension = Dimension(64, 64))
data class Coordinates( val x: Int, val y: Int)
data class Box(val bearing: Float, val coordinates: Coordinates)
data class Robot(val id: String, val name: String, val box: Box)
data class ArenaView(val id: String, val state: ArenaState, val timestamp: Long, val robots: List<Robot>)
enum class ArenaState {
    STARTED, WAITING_FOR_PLAYERS, SIMULATION_RUNNING, OVER, STOPPED;
}
/**
 * The Radar is a projection triangle from the center of the robot towards the end of the screen. It has a ten degree angle between the projection lines, and it's used to find if another robot center is
 * contained within this projection. It's based on Ray Casting algorithms to find if a point is cointained inside a polygon
 *
 * See https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html for reference implementation
 */
class Radar(var center: Vec2, var bearing: Float, var range: Float = 1000.0f) {



    var points : Array<Vec2> = Array(3) { _ -> Vec2(0.0f, 0.0f) }

    init {
        update(center, bearing)
    }

    fun update(center: Vec2, bearing: Float){
        this.bearing = bearing
        points[0] = center
        points[1] = center.moveTo((this.bearing+5), range)
        points[2] = center.moveTo((this.bearing-5), range)
    }

    fun contains(other: Vec2) : Boolean{
        var contains = false
        var i = 0
        var j = points.size - 1
        while (i < points.size) {
            if (points[i].y > other.y != points[j].y > other.y && other.x < (points[j].x - points[i].x) * (other.y - points[i].y) / (points[j].y - points[i].y) + points[i].x) {
                contains = !contains
            }
            j = i++
        }
        return contains
    }
}

/**
 * Moves a point in space considering the distance and angle
 *
 * Using screen coordinate space, therefore Y axis should decrease towards a 90 degree angle
 */
fun Vec2.moveTo(angle: Float, distance: Float)  = Vec2(this.x + (MathUtils.cos(angle) * distance), this.y - (MathUtils.sin(angle)*distance) )

fun normalizeAngle(angle: Double) : Double = angle + Math.ceil( -angle / 360 ) * 360