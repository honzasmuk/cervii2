package org.androworks.cervii2.server

import java.awt.Color
import java.lang.IllegalArgumentException

sealed class ServerMessage {

    data class ClientId(val clientId: Int, val color: Color): ServerMessage()
    data class GameState(val state: Game.State): ServerMessage()
    data class LobbyStatus(val gameInProgress: Boolean, val votingClientIds: List<Int>): ServerMessage()
    data class Players(val players: List<Player>): ServerMessage() {
        data class Player(val id: Int, val name: String, val color: Color, val score: Int)
    }
    data class GameStarted(val clientIds: List<Int>): ServerMessage()
    data class Move(val moves: List<Triple<Int, Int, Int>>): ServerMessage()
    data class RoundFinished(val round: Int, val players: List<Players.Player>): ServerMessage()
    object NextRound: ServerMessage()
    object GameOver: ServerMessage()

    companion object {

        fun parse(string: String): ServerMessage {
            return when (string[0]) {
                'I' -> string.substring(1).split(",").let { ClientId(it[0].toInt(), Color(it[1].toInt())) }
                'G' -> GameState(Game.State.valueOf(string.substring(1)))
                'P' -> Players(string.substring(1).split("|")
                    .map { s -> s.split(",") }.map { Players.Player(it[0].toInt(), it[1], Color(it[2].toInt()), it[3].toInt())})
                'L' -> LobbyStatus(string[1] == '1', string.substring(2).split(",").filter { s -> s.isNotBlank() }.map { it.toInt() })
                'S' -> GameStarted(string.substring(1).split(",").map { it.toInt() })
                'M' -> Move(string.substring(1).split("|").filter {s -> s.isNotBlank() }.map {
                    it.split(",").let { triple -> Triple(triple[0].toInt(), triple[1].toInt(), triple[2].toInt()) }
                })
                'R' -> {
                    val firstCommaIdx = string.indexOf(',')
                    RoundFinished(string.substring(1, firstCommaIdx).toInt(), string.substring(firstCommaIdx+1).split("|")
                        .map { s -> s.split(",") }.map { Players.Player(it[0].toInt(), it[1], Color(it[2].toInt()), it[3].toInt())})
                }
                'N' -> NextRound
                'O' -> GameOver
                else -> throw IllegalArgumentException("unknown message $string")
            }
        }

        fun marshall(message: ServerMessage): String {
            return when (message) {
                is ClientId -> "I${message.clientId},${message.color.rgb}"
                is GameState -> "G${message.state}"
                is Players -> "P" + message.players.map { p -> "${p.id},${p.name},${p.color.rgb},${p.score}" }.joinToString(separator = "|")
                is LobbyStatus -> "L${message.gameInProgress.toInt()}" + message.votingClientIds.joinToString(",")
                is GameStarted -> "S" + message.clientIds.joinToString(",")
                is Move -> "M" + message.moves.joinToString("|") { (id, x, y) -> "$id,$x,$y" }
                is RoundFinished -> "R${message.round}," + message.players.map { p -> "${p.id},${p.name},${p.color.rgb},${p.score}" }.joinToString(separator = "|")
                is NextRound -> "N"
                is GameOver -> "O"
            }
        }

    }
}

fun Boolean.toInt() = if (this) "1" else "0"