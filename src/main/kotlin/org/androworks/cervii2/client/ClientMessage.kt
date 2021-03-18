package org.androworks.cervii2.client

sealed class ClientMessage {

    data class NewClient(val name: String): ClientMessage() {

    }
    object Disconnect: ClientMessage()
    data class Keyboard(val key: Key, val pressed: Boolean): ClientMessage() {
        enum class Key(val code: Char) {
            LEFT('L'), RIGHT('R'), ACTION('A')
        }
    }

    companion object {
        fun parse(messageString: String): ClientMessage {
            return when (messageString[0]) {
                'C' -> NewClient(messageString.substring(1))
                'K' -> Keyboard(
                    when (messageString[1]) {
                        'L' -> Keyboard.Key.LEFT
                        'R' -> Keyboard.Key.RIGHT
                        'A' -> Keyboard.Key.ACTION
                        else -> throw IllegalArgumentException("wrong key code ${messageString[1]}")
                    }, messageString[2] == '1')
                'D' -> Disconnect
                else -> throw IllegalArgumentException("wrong message $messageString")
            }
        }

        fun marshall(message: ClientMessage): String {
            return when (message) {
                is NewClient -> "C${message.name}"
                is Keyboard -> "K${message.key.code}${if(message.pressed) '1' else '0'}"
                is Disconnect -> "D"
            }
        }
    }
}