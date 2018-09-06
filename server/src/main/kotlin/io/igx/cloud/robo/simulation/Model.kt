package io.igx.cloud.robo.simulation

import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import java.util.*

data class EngineConfig(val worldConfig: WorldConfig, val fps: Int = 30, val botConfig: RobotConfig, val bulletConfig: BulletConfig, val simulationConfig: SimulationConfig = SimulationConfig())
data class SimulationConfig(val hitScore: Int = 10, val hitDamage: Int = 25)
data class RobotConfig(val box: Dimension = Dimension(64, 64), val speed: Float, val maxProjectiles: Int, val health: Int)
data class BulletConfig(val box: Dimension = Dimension(6, 6), val speed: Float)
data class Dimension(val width: Int, val height: Int)
data class WorldConfig(val screen: Dimension = Dimension(1024, 768), val scaleFactor: Float)
//START JSON friendly entities
data class Coordinates( val x: Int, val y: Int)
data class Box(val bearing: Float, val coordinates: Coordinates)
data class Robot(val id: String, val name: String, val box: Box, val radar: List<Coordinates> = emptyList(), val health: Int, val score: Int)
data class Projectile(val id: String, val robotId: String, val box: Box)
data class ArenaView(val id: String, val state: ArenaState, val timestamp: Long, val robots: List<Robot>, val projectiles: List<Projectile>)
//END JSON friendly entities
data class BodyData(val type: FixtureType, val context: Map<String, String>)
data class WorldEvent(val type: WorldEventType, val context: Map<String, String>)
enum class ArenaState {
    STARTED, WAITING_FOR_PLAYERS, SIMULATION_RUNNING, OVER, STOPPED;
}
enum class FixtureType {
    WALL, BULLET, ROBOT
}
enum class WorldEventType {
    DESTROY_BULLET, CREATE_BULLET, ROBOT_HIT
}
/**
 * The Radar is a projection triangle from the center of the robot towards the end of the screen. It has a ten degree angle between the projection lines, and it's used to find if another robot center is
 * contained within this projection. It's based on Ray Casting algorithms to find if a point is cointained inside a polygon
 *
 * See https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html for reference implementation
 */
class Radar(var center: Vec2, var bearing: Float, val range: Float = 10.0f) {

    var points : Array<Vec2> = Array(3) { _ -> Vec2(0.0f, 0.0f) }

    init {
        update(center, bearing)
    }

    fun update(center: Vec2, bearing: Float){
        this.bearing = bearing
        points[0] = center
        points[1] = center.moveTo((this.bearing - 5*MathUtils.DEG2RAD), range)
        points[2] = center.moveTo((this.bearing + 5*MathUtils.DEG2RAD), range)
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

interface ArenaCallback {
    fun onFireEvent(robot: ServerRobot)
}

/**
 * Moves a point in space considering the distance and angle
 *
 * Using screen coordinate space, therefore Y axis should decrease towards a 90 degree angle
 */
fun Vec2.moveTo(angle: Float, distance: Float)  = Vec2(this.x + (MathUtils.cos(angle) * distance), this.y + (MathUtils.sin(angle)*distance) )

fun normalizeAngle(angle: Float) : Float = angle + MathUtils.ceil( -angle / 360.0f ) * 360.0f

object RobotNameFactory {

    private val names: List<String> = RobotNameFactory::class.java.classLoader.getResource("robots.txt").readText().split("\n")
    private val random = Random()
    fun getName() : String {
        return names[random.nextInt(names.size)]
    }

}

class CoordinateTranslator(val config:WorldConfig, val scaleFactor:Float = 50.0f){

    val worldWidth = config.screen.width / scaleFactor
    val worldHeight = config.screen.height / scaleFactor

    fun screenToWorld(coordinates: Vec2) : Vec2 {
        val xWorld = coordinates.x/scaleFactor - worldWidth/2
        val yWorld = worldHeight/2 - coordinates.y/scaleFactor
        return Vec2(xWorld, yWorld)
    }

    fun worldToScreen(coordinates: Vec2) : Vec2 {
        val xScreen = config.screen.width/2 + (coordinates.x * scaleFactor)
        val yScreen = config.screen.height/2 - (coordinates.y * scaleFactor)
        return Vec2(xScreen, yScreen)
    }

    fun scaleToScreen(value: Float) : Float{
        return value * scaleFactor
    }

    fun scaleToScreen(value: Int) : Int {
        return scaleToScreen(value.toFloat()).toInt()
    }

    fun scaleToWorld(value: Float) : Float {
        return value / scaleFactor
    }

    fun scaleToWorld(value: Int) : Int {
        return scaleToWorld(value.toFloat()).toInt()
    }

}

object GameHelper {

    val engineConfig: EngineConfig
    val worldWidth: Float
    val worldHeight: Float

    init {
        engineConfig = EngineConfig(WorldConfig(scaleFactor = 50.0f), fps=30, botConfig = RobotConfig(speed = 2.0f, maxProjectiles = 3, health = 100), bulletConfig = BulletConfig(speed = 4.0f))
        worldWidth = engineConfig.worldConfig.screen.width/engineConfig.worldConfig.scaleFactor
        worldHeight = engineConfig.worldConfig.screen.height/engineConfig.worldConfig.scaleFactor
    }

    fun screenToWorld(coordinates: Vec2) : Vec2 {
        val xWorld = coordinates.x/engineConfig.worldConfig.scaleFactor - worldWidth/2
        val yWorld = worldHeight/2 - coordinates.y/engineConfig.worldConfig.scaleFactor
        return Vec2(xWorld, yWorld)
    }

    fun worldToScreen(coordinates: Vec2) : Vec2 {
        val xScreen = engineConfig.worldConfig.screen.width/2 + (coordinates.x * engineConfig.worldConfig.scaleFactor)
        val yScreen = engineConfig.worldConfig.screen.height/2 - (coordinates.y * engineConfig.worldConfig.scaleFactor)
        return Vec2(xScreen, yScreen)
    }

    fun scaleToScreen(value: Float) : Float{
        return value * engineConfig.worldConfig.scaleFactor
    }

    fun scaleToScreen(value: Int) : Int {
        return scaleToScreen(value.toFloat()).toInt()
    }

    fun scaleToWorld(value: Float) : Float {
        return value / engineConfig.worldConfig.scaleFactor
    }

    fun scaleToWorld(value: Int) : Int {
        return scaleToWorld(value.toFloat()).toInt()
    }


}
