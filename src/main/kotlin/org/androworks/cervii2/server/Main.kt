package org.androworks.cervii2.server


class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val connections = Connections()
            val game = Game(connections)
            val lobby = Lobby(connections, game)

            lobby.initiateVoting()

            connections.startListeningForClients(8081)
        }
    }
}