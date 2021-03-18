package org.androworks.cervii2.client

import java.awt.EventQueue
import javax.swing.JFrame
import kotlin.system.exitProcess

class Main(val playerName: String, val server: String, val port: Int): JFrame() {
    init {
        initUI()
    }

    private fun initUI() {
        title = "Cervii 2 - covid edition"
        isResizable = false
        add(GamePanel(playerName, server, port))
        pack()
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server: String
            val port: Int
            val name: String

            if (args.size == 1) {
                server = "aladin2.spekacek.com"
                port = 8081
                name = args[0]
            } else if (args.size == 2){
                name = args[0]
                server = args[1].split(":")[0]
                port = args[1].split(":")[1].toInt()
            } else {
                println("Je treba zadat parametry: <jmeno_bez_mezer> [<server:port>]")
                exitProcess(1)
            }
            EventQueue.invokeLater {
                val main = Main(name, server, port)
                main.isVisible = true
            }
        }
    }
}