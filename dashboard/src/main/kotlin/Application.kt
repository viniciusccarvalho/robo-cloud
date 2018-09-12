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
const val BORDER = 50
const val ANIMATION_WIDTH = 1024
const val ANIMATIION_HEIGHT = 768
const val PANEL_WIDTH = 320
var connected: Boolean = false
val canvas = document.getElementById("myCanvas") as HTMLCanvasElement
val context = canvas.getContext("2d") as CanvasRenderingContext2D
val ship = Sprite(context, "images/ship.png", 64, 64, arrayOf(arrayOf(0, 0)))
val borderWall = Sprite(context,
        "images/tilesetpr.png",
        50,
        50,
        arrayOf(
                arrayOf(122, 7), //bevel
                arrayOf(7, 72),  //plain
                arrayOf(64, 72)) // corner
)
val canvasWindow = CanvasWindow(sprite = borderWall)
val parallax = Parallax(ctx = context)
val statusWindow = StatusWindow(context, BORDER*2+ANIMATION_WIDTH, BORDER)

fun main(args: Array<String>) {





    window.onload = {

        canvasWindow.draw()

    }
    jq("#connectButton").click {
        val button = jq("#connectButton")
        connected = !connected
        if(connected){
            val socket = WebSocket("ws://localhost:8080/arenas")
            socket.onmessage = { event ->
                val message  = event.unsafeCast<MessageEvent>()
                val view: ArenaView = JSON.parse(message.data.toString())
                onViewUpdate(view)
            }
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

fun onViewUpdate(view: ArenaView) {
    parallax.draw()
    view.robots.forEach { bot ->
        ship.draw(0, bot.box.coordinates.x -32+50, bot.box.coordinates.y -32+50 , bot.box.bearing.toDouble())
        statusWindow.updateBot(bot)

    }
    statusWindow.render()
    canvasWindow.draw()
}

class Sprite(val ctx: CanvasRenderingContext2D, val path: String, val width: Int, val height: Int, val positions: Array<Array<Int>>) {

    val image: HTMLImageElement = window.document.createElement("img") as HTMLImageElement

    init {
        image.src = path
    }

    fun draw(position: Int, x: Int, y: Int, angle: Double){
        var pos = positions[position]

        ctx.save()
        ctx.translate(x + this.width/2.0, y + this.height/2.0)
        ctx.rotate(angle)
        ctx.drawImage(this.image,
        pos[0].toDouble(),
        pos[1].toDouble(),
        this.width.toDouble(),
        this.height.toDouble(),
        -this.width/2.0,
        -this.height/2.0,
        this.width.toDouble(),
        this.height.toDouble())

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

class StatusWindow(val ctx: CanvasRenderingContext2D, val marginLeft: Int, val marginTop : Int) {
    var bots = mutableMapOf<String, BotStatus>()

    fun render(){
        ctx.clearRect(marginLeft.toDouble(), marginTop.toDouble(), PANEL_WIDTH.toDouble(), ANIMATIION_HEIGHT.toDouble())
        bots.values.forEach { bot -> bot.render(ctx) }
    }

    fun updateBot(bot: Robot) {

        if(bots.containsKey(bot.id)){
            bots[bot.id]?.robotState = bot
        }else{
            bots[bot.id] = BotStatus(bot, bots.size, marginLeft, marginTop)
        }

    }
}

class BotStatus(var robotState: Robot, val index: Int, val marginLeft: Int, val marginTop: Int) {

    val margin = 10
    val height = 50
    val fontHeight = 9.0
    val maxHealthSize = 100
    var targetHealthSize = maxHealthSize
    val barWidth = 102.0
    val barHeight = 14.0
    val healthHeight = 12.0


    fun render(ctx: CanvasRenderingContext2D) {
        drawEnergyBar(ctx)
        drawText(ctx)
    }

    private fun drawEnergyBar(ctx: CanvasRenderingContext2D){
        val currentHealthSize = (robotState.health.toDouble()/100) * maxHealthSize
        val barMarginTop = marginTop + (height * index) + 18 + fontHeight
        if((targetHealthSize - currentHealthSize) >= 0){
            targetHealthSize = max(0, --targetHealthSize)
        }
        ctx.save()
        ctx.strokeStyle = "#ffffff"
        ctx.strokeRect(marginLeft + 80.0,  barMarginTop, barWidth, barHeight)
        ctx.fillStyle = "#ff0000"
        ctx.fillRect(marginLeft + 81.0, barMarginTop+1, maxHealthSize.toDouble(), healthHeight)
        ctx.fillStyle = "#ff0000"
        ctx.fillRect(marginLeft + 81.0, barMarginTop+1, targetHealthSize.toDouble() + 1, healthHeight)
        ctx.fill()
        ctx.stroke()
        ctx.restore()
    }

    private fun drawText(ctx: CanvasRenderingContext2D){
        val rightColumnOffset = marginLeft + PANEL_WIDTH - margin - 100.0
        val leftColumnOffset = margin + marginLeft.toDouble()
        val firstRowOffset = marginTop + margin + fontHeight + (height * index)
        val secondRowOffset = marginTop + margin + fontHeight + (height * index) + 20.0
        ctx.save()
        ctx.fillStyle = "#ffffff"
        ctx.font = "8px \"Press Start 2P\""
        ctx.fillText(robotState.name, leftColumnOffset, firstRowOffset)
        ctx.fillText("Health :", leftColumnOffset, secondRowOffset)
        ctx.fillText("Score : " + formatScore(robotState.score), rightColumnOffset, firstRowOffset)
        ctx.fillText("Ammo  :", rightColumnOffset, secondRowOffset)
        ctx.fill()
        ctx.restore()
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
    }

    private fun inBounds(position : Position) : Boolean{
        return ((position.x > BORDER && position.x < BORDER+ANIMATION_WIDTH) && (position.y > BORDER && position.y < BORDER+ANIMATIION_HEIGHT))
    }
}

fun formatScore(score: Int) : String{
    val scoreText = score.toString()
    val builder = StringBuilder()
    val length = scoreText.length
    for (i in length..4){
        builder.append("0")
    }
    builder.append(scoreText)
    return builder.toString()
}

class Position(var x: Double = 0.0, var y: Double = 0.0)
class Star(var position: Position, var radius: Double = 0.0, var speed: Position)
data class Coordinates( val x: Int, val y: Int)
data class Box(val bearing: Float, val coordinates: Coordinates)
data class Robot(val id: String, val name: String, val box: Box, val radar: List<Coordinates> = emptyList(), val health: Int, val score: Int)
data class Projectile(val id: String, val robotId: String, val box: Box)
data class ArenaView(val id: String, val state: ArenaState, val timestamp: Long, val robots: Array<Robot>, val projectiles: Array<Projectile>)
enum class ArenaState {
    STARTED, WAITING_FOR_PLAYERS, SIMULATION_RUNNING, OVER, STOPPED;
}

fun ClosedRange<Int>.random() = (Math.random() * ((endInclusive + 1) - start) + start).toInt()
