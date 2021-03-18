package org.androworks.cervii2.client

import org.androworks.cervii2.server.ServerMessage
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class ClientConnection(val socket: Socket) {
    var active = true
    val sender = Sender(socket.getOutputStream().bufferedWriter())
    val receiver = Receiver(socket.getInputStream().bufferedReader())
    val listeners = Collections.synchronizedList(mutableListOf<MessageListener>())

    fun sendMessage(message: ClientMessage) {
        sender.send(message)
    }

    init {
        Thread(sender, "server-sender").start()
        Thread(receiver, "server-receiver").start()
    }

    inner class Sender(val output: BufferedWriter): Runnable {
        val messagesToSend = ArrayBlockingQueue<ClientMessage>(5)
        override fun run() {
            while(active) {
                val message = messagesToSend.take()
                try {
                    output.appendLine(ClientMessage.marshall(message))
                    output.flush()
                } catch (e: Exception) {
                    log.warn("error while writing to socket: {}", e.message)
                    active = false
                }
            }
            output.close()
        }

        fun send(message: ClientMessage) {
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
                    val message = ServerMessage.parse(line)
                    if (log.isDebugEnabled) log.debug("received message: {}", message)
                    listeners.forEach { it.onMessage(message) }
                }
            } catch (e: Exception) {
                log.warn("error while reading from socket: {}", e.message)
                active = false
            }
            input.close()
        }
    }

    interface MessageListener {
        fun onMessage(message: ServerMessage)
    }

    companion object {
        val log = LoggerFactory.getLogger(ClientConnection::class.java)
    }
}