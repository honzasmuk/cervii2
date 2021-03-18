package org.androworks.cervii2.client

import org.androworks.cervii2.server.ServerMessage
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.net.Socket
import javax.swing.JPanel


class GamePanel(playerName: String, server: String, port: Int) : JPanel(), KeyListener, ClientConnection.MessageListener {
    private val mapWidth = 640
    private val mapHeight = 480
    private var leftPressed = false
    private var rightPressed = false
    private var actionPressed = false
    private val game: Game
    private val lobby: Lobby
    private var lastFrameTime: Long = 0
    val connection: ClientConnection

    init {
        preferredSize = Dimension(mapWidth, mapHeight)
        background = Color.black
        isFocusable = true
        addKeyListener(this)
        val socket = Socket(server, port)
        connection = ClientConnection(socket)
        connection.listeners.add(this)
        game = Game(mapWidth, mapHeight, this)
        lobby = Lobby(connection, this, game)
        connection.sendMessage(ClientMessage.NewClient(playerName))
        lastFrameTime = System.currentTimeMillis()
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (lobby.isCurrentPlayerInLobby()) {
            lobby.drawLobby(g)
        } else {
            drawGame(g)
        }
    }

    fun startGame(players: List<ServerMessage.Players.Player>) {
        game.startGame(players)
    }

    private fun drawGame(g: Graphics) {
        g.drawImage(game.map, 0, 0, this)
        Toolkit.getDefaultToolkit().sync()
    }

    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_LEFT -> if (!leftPressed) {
                leftPressed = true
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.LEFT, true))
            }
            KeyEvent.VK_RIGHT -> if (!rightPressed) {
                rightPressed = true
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.RIGHT, true))
            }
            KeyEvent.VK_SPACE -> if (!actionPressed) {
                actionPressed = true
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.ACTION, true))
            }
        }
    }

    override fun keyReleased(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_LEFT -> if (leftPressed) {
                leftPressed = false
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.LEFT, false))
            }
            KeyEvent.VK_RIGHT -> if (rightPressed) {
                rightPressed = false
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.RIGHT, false))
            }
            KeyEvent.VK_SPACE -> if (actionPressed) {
                actionPressed = false
                connection.sendMessage(ClientMessage.Keyboard(ClientMessage.Keyboard.Key.ACTION, false))
            }
        }
    }

    override fun onMessage(message: ServerMessage) {
        when (message) {
            is ServerMessage.Move -> {
                game.nextMove(message)
                repaint()
            }
            is ServerMessage.NextRound -> {
                game.nextRound()
            }
            is ServerMessage.GameOver -> {
                game.inProgress = false
                repaint()
            }
        }
    }

}