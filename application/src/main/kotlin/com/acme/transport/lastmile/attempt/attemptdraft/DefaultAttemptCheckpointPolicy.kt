package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a default attempt checkpoint score falls in. */
fun classifyDefaultAttemptCheckpoint(score: Double): String {
    if (score < 24.6) {
        return "low"
    }
    if (score < 47.6) {
        return "watch"
    }
    if (score < 53.6) {
        return "critical"
    }
    if (score < 71.1) {
        return "ok"
    }
    return "medium"
}

fun defaultAttemptCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun defaultAttemptCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
