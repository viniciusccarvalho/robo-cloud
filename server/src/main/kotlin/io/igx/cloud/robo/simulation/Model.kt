package io.igx.cloud.robo.simulation

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D


data class Box(val width: Int, val height: Int)
data class WorldConfig(val screen: Box = Box(1024, 768), val botBox : Box = Box(64, 64))

/**
 * The Radar is a projection triangle from the center of the robot towards the end of the screen. It has a ten degree angle between the projection lines, and it's used to find if another robot center is
 * contained within this projection. It's based on Ray Casting algorithms to find if a point is cointained inside a polygon
 *
 * See https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html for reference implementation
 */
class Radar(var center: Vector2D, var bearing: Double, var range: Double = 1000.0) {
    var points : Array<Vector2D> = Array(3) { _ -> Vector2D(0.0, 0.0) }

    init {
        update(center, bearing)
    }

    fun update(center: Vector2D, bearing: Double){
        points[0] = center
        points[1] = center.moveTo(Math.toRadians(bearing+5), range)
        points[2] = center.moveTo(Math.toRadians(bearing-5), range)
    }

    fun contains(other: Vector2D) : Boolean{
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
 */
fun Vector2D.moveTo(angle: Double, distance: Double)  = Vector2D(this.x + (Math.cos(angle) * distance), this.y + (Math.sin(angle)*distance) )

fun normalizeAngle(angle: Double) : Double = angle + Math.ceil( -angle / 360 ) * 360