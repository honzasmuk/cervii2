package org.androworks.cervii2.client

import org.androworks.cervii2.server.ServerMessage
import java.awt.Color
import java.awt.Graphics

class Lobby(val connection: ClientConnection, val gamePanel: GamePanel, val game: Game): ClientConnection.MessageListener {

    init {
        connection.listeners.add(this)
    }

    var players = emptyList<ServerMessage.Players.Player>()
    var votingPlayers = emptyList<Int>()
    var currentPlayerId = 0
    var lastRound: ServerMessage.RoundFinished? = null

    override fun onMessage(message: ServerMessage) {
        when (message) {
            is ServerMessage.Players -> {
                players = message.players
                gamePanel.repaint()
            }

            is ServerMessage.GameStarted -> {
                gamePanel.startGame(players.filter { p -> p.id in message.clientIds})
            }

            is ServerMessage.LobbyStatus -> {
                votingPlayers = message.votingClientIds
                gamePanel.repaint()
            }

            is ServerMessage.ClientId -> {
                currentPlayerId = message.clientId
            }

            is ServerMessage.RoundFinished -> {
                lastRound = message
                gamePanel.repaint()
            }


        }
    }

    fun isCurrentPlayerInLobby(): Boolean {
        return !game.inProgress || currentPlayerId == 0 || !game.players.containsKey(currentPlayerId)
    }

    fun drawLobby(g: Graphics) {
        g.color = Color.black
        g.fillRect(0, 0, gamePanel.width, gamePanel.height)
        val sortedPlayers = mutableListOf<ServerMessage.Players.Player>()
        players.firstOrNull { p -> p.id == currentPlayerId }?.let { sortedPlayers.add(it) }
        sortedPlayers.addAll(players.filter { p -> p.id != currentPlayerId })
        g.color = Color.white
        g.drawString("LOBBY:", 10, 20)
        sortedPlayers.forEachIndexed { index, player ->
            g.color = player.color
            g.drawString(player.name, 10, 40 + 20*index)
            if (!game.inProgress && votingPlayers.contains(player.id)) {
                g.drawString("Startuju !!!", 200, 40 + 20*index)
            }
        }

        g.color = Color.white
        if (game.inProgress) {
            g.drawString("Prave se hraje. Cekej, az hra skonci....", 10, 470)
        } else {
            g.drawString("Zmackni a drz MEZERU, pokud chces zacit hrat. Hra zacne pokud budou stejni hraci drzet mezeru 4 sekundy.", 10, 470)
        }

        lastRound?.let { round ->
            g.color = Color.white
            g.drawString("Posledni hra (kolo: ${round.round})", 400, 20)
            round.players.sortedByDescending { p -> p.score }.forEachIndexed { index, player ->
                g.color = player.color
                g.drawString(player.name, 400, 40 + 20*index)
                g.drawString(player.score.toString(), 500, 40 + 20*index)
            }
        }


    }


}