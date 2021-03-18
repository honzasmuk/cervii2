package org.androworks.cervii2.server

import org.androworks.cervii2.client.ClientMessage
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue

class ServerConnection(val clientId: Int, val socket: Socket, val listener: MessageListener) {
    var active = true
    val sender = Sender(socket.getOutputStream().bufferedWriter())
    val receiver = Receiver(socket.getInputStream().bufferedReader())

    fun sendMessage(message: ServerMessage) {
        sender.send(message)
    }

    init {
        Thread(sender, "client-sender-$clientId").start()
        Thread(receiver, "client-receiver-$clientId").start()
    }

    inner class Sender(val output: BufferedWriter): Runnable {
        val messagesToSend = ArrayBlockingQueue<ServerMessage>(5)
        override fun run() {
            while(active) {
                val message = messagesToSend.take()
                log.debug("sending message: $message")
                try {
                    val marshalled = ServerMessage.marshall(message)
                    log.debug("marshalled: $marshalled")
                    output.appendLine(marshalled)
                    output.flush()
                } catch (e: Exception) {
                    log.warn("error while writing to socket: {}", e.message)
                    active = false
                }
            }
            output.close()
        }

        fun send(message: ServerMessage) {
            while(messagesToSend.remainingCapacity() == 0) {
                messagesToSend.remove()
            }
            messagesToSend.add(message)
        }
    }

    inner class Receiver(val input: BufferedReader): Runnable {
        override fun run() {
            try {
                input.forEachLine { line ->
                    val message = ClientMessage.parse(line)
                    if (log.isDebugEnabled) log.debug("received message from client {}: {}", clientId, message)
                    listener.onMessage(clientId, message)
                }
            } catch (e: Exception) {
                log.warn("error while reading from socket: {}", e.message)
                active = false
            }
            listener.onMessage(clientId, ClientMessage.Disconnect)
            input.close()
        }
    }

    interface MessageListener {
        fun onMessage(clientId: Int, message: ClientMessage)
    }

    companion object {
        val log = LoggerFactory.getLogger(ServerConnection::class.java)
    }
}