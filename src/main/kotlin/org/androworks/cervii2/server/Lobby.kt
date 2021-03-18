package org.androworks.cervii2.server

import org.androworks.cervii2.client.ClientMessage
import java.awt.Color
import java.util.*

class Lobby(val connections: Connections, val game: Game): ServerConnection.MessageListener {

    val players = Collections.synchronizedMap(mutableMapOf<Int, LobbyPlayer>())
    val timer = Timer()

    init {
        connections.listeners.add(this)
    }

    fun initiateVoting() {
        var lastGroupStartTime: Long = 0
        var lastGroup: List<Int> = emptyList()
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                val votingPlayers = players.values.filter { p -> p.pressed }
                val newGroup = votingPlayers.map(LobbyPlayer::id).sorted()
                if (!game.inProgress && votingPlayers.size >= 2) {
                    if (lastGroup == newGroup) {
                        if (System.currentTimeMillis() - lastGroupStartTime > 4_000) {
                            game.startGame(players.values.filter { p -> p.id in lastGroup }.toList())
                            connections.sendToAll(ServerMessage.GameStarted(lastGroup))
                        }
                    } else {
                        lastGroupStartTime = System.currentTimeMillis()
                    }
                }
                lastGroup = newGroup
                connections.sendToAll(ServerMessage.LobbyStatus(game.inProgress, lastGroup))
            }

        }, 1000L, 1000L)
    }


    override fun onMessage(clientId: Int, message: ClientMessage) {
        when(message) {
            is ClientMessage.NewClient -> {
                val player = LobbyPlayer(
                    clientId,
                    message.name,
                    Color(random.nextInt(20, 250), random.nextInt(20, 250), random.nextInt(20, 250)))

                players[clientId] = player
                connections.send(clientId, ServerMessage.ClientId(clientId, player.color))
                connections.sendToAll(ServerMessage.Players(players.values.map { ServerMessage.Players.Player( it.id, it.name, it.color, 0) }))
            }
            is ClientMessage.Keyboard -> {
                players[clientId]?.pressed = message.pressed
            }
            is ClientMessage.Disconnect -> {
                players.remove(clientId)
                connections.sendToAll(ServerMessage.Players(players.values.map { ServerMessage.Players.Player( it.id, it.name, it.color, 0) }))
            }
            else -> {}
        }
    }



    data class LobbyPlayer(val id: Int, val name: String, val color: Color, var pressed: Boolean = false)

}