package org.androworks.cervii2.client

import java.awt.Color
import java.awt.geom.Point2D
import kotlin.math.cos
import kotlin.math.sin

class Worm(val name: String, val color: Color) {
    var head = Point2D.Float()
    var speed = 0f
    var angle = 0.0
    var leftPressed = false
    var rightPressed = false
    var points = mutableListOf<Point2D.Float>()
    var active = false

    fun activate(location: Point2D.Float, speed: Float, angle: Double) {
        this.head = location
        this.speed = speed
        this.angle = angle
        points.clear()
        leftPressed = false
        rightPressed = false
        active = true
    }

    fun move(game: Game, deltaTime: Long) {
        if (leftPressed) angle += 5
        else if (rightPressed) angle -= 5

        val dx = (deltaTime*speed*cos(Math.toRadians(angle))).toFloat()
        val dy = - (deltaTime*speed*sin(Math.toRadians(angle))).toFloat()

        points.add(head)
        head = Point2D.Float(head.x + dx, head.y + dy)
    }
}