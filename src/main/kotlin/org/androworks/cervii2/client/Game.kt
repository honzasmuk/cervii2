package org.androworks.cervii2.client

import org.androworks.cervii2.server.ServerMessage
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.random.Random

class Game(val width: Int, val height: Int, gamePanel: GamePanel) {

    val worms = mutableListOf<Worm>()

    var gameTime = 0L
    val random = Random(System.currentTimeMillis())
    val map = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val mapCanvas = (map.createGraphics() as Graphics2D).apply {
        val rh = RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        rh[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
        setRenderingHints(rh)
        stroke = BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
    }
    var inProgress = false
    var players = mapOf<Int, GamePlayer>()


    fun startGame(players: List<ServerMessage.Players.Player>) {
        mapCanvas.clearRect(0, 0, width, height)
        inProgress = true
        this.players = players.map { p -> GamePlayer(p.id, p.name, p.color) }.associateBy { p -> p.id }
    }

    fun nextMove(move: ServerMessage.Move) {
        move.moves.forEach { (clientId, x, y) ->
            val player = players[clientId]
            if (player != null) {
                if (player.x != -1) {
                    mapCanvas.color = player.color
                    mapCanvas.drawLine(player.x, player.y, x, y)
                }
                player.x = x
                player.y = y
            }

        }
    }

    fun nextRound() {
        mapCanvas.clearRect(0, 0, width, height)
        players.values.forEach { p -> p.x = -1 }
    }


    data class GamePlayer(val id: Int,
                          val name: String,
                          val color: Color,
                          var x: Int = -1,
                          var y: Int = -1,
                          var score: Int = 0)

}