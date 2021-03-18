package org.androworks.cervii2.server

import org.androworks.cervii2.client.ClientMessage
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Game(val connections: Connections): ServerConnection.MessageListener {

    val width = 640
    val height = 480
    val players = Collections.synchronizedMap(mutableMapOf<Int, Player>())
    val map = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val mapCanvas = map.createGraphics()
    var state = State.LOBBY
    val random = Random(System.currentTimeMillis())
    var lastTickTime = 0L
    var inProgress = false
    var finishedRounds = 0
    var timer: Timer? = null

    init {
        connections.listeners.add(this)
    }


    fun startGame(players: List<Lobby.LobbyPlayer>) {
        finishedRounds = 0
        mapCanvas.stroke = BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
        this.players.clear()
        this.players.putAll(players.map { p -> Player(p.id, p.name, p.color) }.associateBy { p -> p.id } )
        placePlayers()
        inProgress = true

        lastTickTime = System.currentTimeMillis()
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                gameTick()
            }
        }, 60, 60)
        this.timer = timer
    }

    fun restartRound() {
        mapCanvas.clearRect(0, 0, width, height)
        placePlayers()
        connections.sendToAll(ServerMessage.NextRound)
    }

    private fun placePlayers() {
        players.forEach { (_, p) ->
            p.apply {
                active = true
                position.setLocation(random.nextDouble(80.0, 400.0), random.nextDouble(80.0, 400.0))
                angle = random.nextDouble(360.0)
                speed = random.nextDouble(0.03, 0.06).toFloat() }
        }
    }


    override fun onMessage(clientId: Int, message: ClientMessage) {
        when (message) {
            is ClientMessage.Keyboard -> {
                when (message.key) {
                    ClientMessage.Keyboard.Key.LEFT -> players[clientId]?.leftPressed = message.pressed
                    ClientMessage.Keyboard.Key.RIGHT -> players[clientId]?.rightPressed = message.pressed
                    else -> {}
                }
            }
        }
    }

    private fun gameTick() {
        val now = System.currentTimeMillis()
        val deltaTime = now - lastTickTime
        players.forEach { (_, player) ->
            player.move(this, deltaTime)
        }
        val activePlayers = players.values.filter { p -> p.active }
        connections.sendToAll(ServerMessage.Move(activePlayers.map { p -> Triple(p.id, p.position.x.toInt(), p.position.y.toInt())}))
        lastTickTime = now
        if (activePlayers.size < 2) {
            roundEnd(activePlayers.firstOrNull())
        }
    }

    fun roundEnd(player: Player?) {
        if (player != null) player.score ++
        finishedRounds ++
        connections.sendToAll(ServerMessage.RoundFinished(finishedRounds, players.values.map { p -> ServerMessage.Players.Player(p.id, p.name, p.color, p.score)}))
        if (finishedRounds == 10) {
            gameOver()
        } else {
            restartRound()
        }
    }

    fun gameOver() {
        timer?.cancel()
        inProgress = false
        connections.sendToAll(ServerMessage.GameOver)
    }


    enum class State {
        LOBBY, PROGRESS
    }



    class Player(
        val id: Int,
        val name: String,
        val color: Color,
        var score: Int = 0,
        var active: Boolean = false,
        var leftPressed: Boolean = false,
        var rightPressed: Boolean = false,
        var position: Point2D = Point2D.Float(0f, 0f),
        var angle: Double = 0.0,
        var speed: Float = 0f,
        var status: Char = 'L') {

        fun move(game: Game, deltaTime: Long) {
            if (!active) return
            if (leftPressed) angle += 8
            else if (rightPressed) angle -= 8

            val dx = (deltaTime*speed* cos(Math.toRadians(angle))).toFloat()
            val dy = - (deltaTime*speed* sin(Math.toRadians(angle))).toFloat()
            val newX = position.x + dx
            val newY = position.y + dy
            if (newX < 0 || newX >= game.width || newY < 0 || newY >= game.height
                || game.map.getRGB(newX.toInt(), newY.toInt()) != black) {

                active = false
            }
            game.mapCanvas.drawLine(position.x.toInt(), position.y.toInt(), newX.toInt(), newY.toInt())
            position.setLocation(newX, newY)

        }

    }

    companion object {
        val black = 0xFF000000.toInt()
    }
}