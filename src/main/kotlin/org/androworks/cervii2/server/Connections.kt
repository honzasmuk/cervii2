package org.androworks.cervii2.server

import org.androworks.cervii2.client.ClientMessage
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Connections : ServerConnection.MessageListener {

    val connections = Collections.synchronizedMap(mutableMapOf<Int, ServerConnection>())
    val listeners = mutableListOf<ServerConnection.MessageListener>()

    fun startListeningForClients(port: Int) {
        ServerSocket(port).use { serverSocket ->
            log.info("start listening for tcp connections on port {}", port)
            while (true) {
                val clientSocket = serverSocket.accept()
                val clientId = clientIdSequence.incrementAndGet()
                log.info("new client connected: id={}", clientId)
                connections[clientId] = ServerConnection(clientId, clientSocket, this)
            }
        }
    }

    fun sendToAll(message: ServerMessage) {
        connections.forEach { (_, connection) -> connection.sendMessage(message) }
    }

    fun send(clientId: Int, message: ServerMessage) {
        connections[clientId]?.sendMessage(message)
    }


    val log = LoggerFactory.getLogger(Connections::class.java)
    val clientIdSequence = AtomicInteger(1)


    override fun onMessage(clientId: Int, message: ClientMessage) {
        if (message is ClientMessage.Disconnect) {
            connections.remove(clientId)
        }
        listeners.forEach { listener -> listener.onMessage(clientId, message) }

    }

}