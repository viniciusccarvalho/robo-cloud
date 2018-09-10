import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Math
import kotlin.math.*
import jquery.*

/**
 * @author vinicius
 *
 */
val BORDER = 50
val ANIMATION_WIDTH = 1024
val ANIMATIION_HEIGHT = 768
val PANEL_WIDTH = 320
var connected: Boolean = false

fun main(args: Array<String>) {
    val canvas = document.getElementById("myCanvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    val borderWall = Sprite(context,
            "images/tilesetpr.png",
            50,
            50,
            arrayOf(
                    arrayOf(122, 7), //bevel
                    arrayOf(7, 72),  //plain
                    arrayOf(64, 72)) // corner
            )
    val ship = Sprite(context, "images/ship.png", 64, 64, arrayOf(arrayOf(0, 0)))
    val parallax = Parallax(ctx = context)

    window.onload = {
        val canvasWindow = CanvasWindow(sprite = borderWall)
        canvasWindow.draw()
        parallax.draw()
    }
    jq("#connectButton").click {
        val button = jq("#connectButton")
        connected = !connected
        if(connected){
            val socket = WebSocket("ws://localhost:8080/arenas")
            socket.onmessage = { event ->  }
            connected = true
            button.text("Disconnect")
            button.removeClass("btn-primary")
            button.addClass("btn-danger")

        }else{
            connected = false
            button.text("Connect")
            button.removeClass("btn-danger")
            button.addClass("btn-primary")
        }
    }


}

class Sprite(val ctx: CanvasRenderingContext2D, val path: String, val width: Int, val height: Int, val positions: Array<Array<Int>>) {

    val image: HTMLImageElement = window.document.createElement("img") as HTMLImageElement

    init {
        image.src = path
    }

    fun draw(position: Int, x: Int, y: Int, angle: Double){
        var pos = positions[position]
        ctx.save()
        if(angle != 0.0){
            ctx.translate(x + this.width/2.0, y + this.height/2.0)
            ctx.rotate(angle)
            ctx.drawImage(this.image, pos[0].toDouble(), pos[1].toDouble(), this.width.toDouble(), this.height.toDouble(), -this.width/2.0, -this.width/2.0, this.width.toDouble(), this.height.toDouble())

        }else{
            ctx.drawImage(
                    this.image,
                    pos[0].toDouble(),
                    pos[1].toDouble(),
                    this.width.toDouble(),
                    this.height.toDouble(),
                    x.toDouble(), y.toDouble(),
                    this.width.toDouble(),
                    this.height.toDouble()
            )
        }
        ctx.restore()
    }

}

class CanvasWindow(val height: Int = 768, val battleWidth: Int = 1024, val panelWidth : Int = 320, val border: Int = 50, val sprite: Sprite) {

    val totalWidth = battleWidth + panelWidth + 3 * border
    val totalHeight = height + 2 * border

    fun draw() {
        for(i in border .. (totalWidth-border) step border){
            sprite.draw(0, i, 0, 0.0)
            sprite.draw(0, i, totalHeight-border, 180.0f * PI/180.0f)
        }
        for(j in border .. (totalHeight-border) step border){
            sprite.draw(0, 0, j, -90.0 * PI/180.0f)
            sprite.draw(0, totalWidth-border, j, 90.0 * PI/180.0f)
            sprite.draw(1, battleWidth+BORDER, j, 0.0)
        }
        drawCorners()

    }

    private fun drawCorners(){
        sprite.draw(2, 0, 0, -90.0 * PI/180.0f)
        sprite.draw(2, totalWidth - border, 0, 0.0)
        sprite.draw(2, 0, totalHeight-border, 180.0 * PI/180.0f)
        sprite.draw(2, totalWidth - border, totalHeight-border, 90.0 * PI/180.0f)
    }
}

class Parallax(val numLayers: Int = 12, val baseStar: Int = 6, val ctx: CanvasRenderingContext2D ) {
    var started: Boolean = false
    val bgLayers = mutableListOf<List<Star>>()

    private fun initBackground() {
        for(i in numLayers downTo 0){
            val stars = mutableListOf<Star>()
            for(j in 0..baseStar*i){
                val star = Star(Position( (BORDER..ANIMATION_WIDTH+BORDER).random().toDouble(), (BORDER..ANIMATIION_HEIGHT+BORDER).random().toDouble()), 1.0 / i, Position(0.0, 2.0/i))
                stars.add(star)
            }
            bgLayers.add(stars)
        }

    }

    fun render(){
        ctx.fillStyle = "#000000";
        ctx.strokeStyle = "#FFFFFF";
        ctx.fillRect(BORDER.toDouble() + 1.0, BORDER.toDouble() + 1.0, ANIMATION_WIDTH.toDouble() - 1.0, ANIMATIION_HEIGHT.toDouble() -1.0)
        ctx.save()
        ctx.strokeStyle = "#FFFFFF"
        ctx.fillStyle = "#FFFFFF"
        for(i in 0 until bgLayers.size){
            val stars = bgLayers[i]
            for(j in 0 until stars.size){
                val star = stars[j]
                ctx.beginPath()
                ctx.arc(star.position.x, star.position.y, star.radius, 0.0, Math.PI * 2)
                ctx.fill()
                ctx.stroke()
            }
        }
        ctx.restore()
    }

    fun update(delta: Long){
        if(!started){
            started = true
            initBackground()
        }else{
            for(i in 0 until bgLayers.size){
                val stars = bgLayers[i]
                for(j in 0 until stars.size){
                    val star = stars[j]
                    if(inBounds(star.position)){
                        star.position.x += star.speed.x
                        star.position.y += star.speed.y
                    }else{
                        star.position.y = 1.0 + BORDER
                        star.position.x = (BORDER..ANIMATION_WIDTH+BORDER).random().toDouble()
                    }
                }
            }
        }
    }

    fun draw(){
        update(0L)
        render()
        window.setTimeout({draw()}, 30)
    }

    private fun inBounds(position : Position) : Boolean{
        return ((position.x > BORDER && position.x < BORDER+ANIMATION_WIDTH) && (position.y > BORDER && position.y < BORDER+ANIMATIION_HEIGHT))
    }
}

class Position(var x: Double = 0.0, var y: Double = 0.0)
class Star(var position: Position, var radius: Double = 0.0, var speed: Position)
data class Coordinates( val x: Int, val y: Int)
data class Box(val bearing: Float, val coordinates: Coordinates)
data class Robot(val id: String, val name: String, val box: Box, val radar: List<Coordinates> = emptyList(), val health: Int, val score: Int)
data class Projectile(val id: String, val robotId: String, val box: Box)
data class ArenaView(val id: String, val state: ArenaState, val timestamp: Long, val robots: List<Robot>, val projectiles: List<Projectile>)
enum class ArenaState {
    STARTED, WAITING_FOR_PLAYERS, SIMULATION_RUNNING, OVER, STOPPED;
}

fun ClosedRange<Int>.random() = (Math.random() * ((endInclusive + 1) - start) + start).toInt()
