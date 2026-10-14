package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a cross dock window score falls in. */
fun classifyCrossDockWindow(score: Double): String {
    if (score < 21.6) {
        return "minimal"
    }
    if (score < 22.8) {
        return "ok"
    }
    if (score < 33.5) {
        return "watch"
    }
    if (score < 80.0) {
        return "high"
    }
    return "medium"
}

fun crossDockWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun crossDockWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
