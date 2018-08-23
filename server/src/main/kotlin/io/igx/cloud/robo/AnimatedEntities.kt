package io.igx.cloud.robo

/**
 * @author vinicius
 *
 */

interface Movable {
    fun update(delta: Int)
}

data class Point(val x: Int, val y: Int)