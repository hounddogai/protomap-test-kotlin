package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dwellWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a dwell window score falls in. */
fun classifyDwellWindow(score: Double): String {
    if (score < 1.0) {
        return "watch"
    }
    if (score < 13.3) {
        return "ok"
    }
    if (score < 17.8) {
        return "medium"
    }
    return "low"
}

fun dwellWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
