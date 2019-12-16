package com.anwesh.uiprojects.ballupbouncyview

/**
 * Created by anweshmishra on 16/12/19.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val parts : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 4.3f
val delay : Long = 30
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify(n : Int) : Float = Math.sin(Math.PI * n.inverse() * this).toFloat()

fun Canvas.drawBall(scale : Float, gap : Float, paint : Paint) {
    val size : Float = gap / sizeFactor
    val sf1 : Float = scale.sinify(1)
    var sf2 : Float = scale.sinify(1)
    if (scale > 0.5f) {
        sf2 = 1f - sf2
    }
    drawCircle(gap * sf2, -gap * sf1, size, paint)
}

fun Canvas.drawTriLine(scale : Float, gap : Float, paint : Paint) {
    val sf1 : Float = scale.divideScale(0, 2).sinify(2)
    val sf2 : Float = scale.divideScale(1, 2).sinify(2)
    drawLine(0f, 0f, (gap / 2) * sf1, -gap * sf1, paint)
    drawLine(gap / 2, -gap, gap / 2 + (gap / 2) * sf2, -gap + gap * sf2, paint)
}

fun Canvas.drawBUBNode(i : Int, scale : Float, curr : Boolean, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    save()
    translate(gap * (i + 1), h / 2)
    drawTriLine(scale, gap, paint)
    if (curr) {
        drawBall(scale, gap, paint)
    }
    restore()
}

class BallUpBouncyView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () ->  Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BUBNode(var i : Int, val state : State = State()) {

        private var next : BUBNode? = null
        private var prev : BUBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BUBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, currI : Int, paint : Paint) {
            canvas.drawBUBNode(i, state.scale, currI == i, paint)
            prev?.draw(canvas, currI, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BUBNode {
            var curr : BUBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }
}