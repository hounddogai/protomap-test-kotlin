package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun crossDockOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun crossDockOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a cross dock override score falls in. */
fun classifyCrossDockOverride(score: Double): String {
    if (score < 22.6) {
        return "minimal"
    }
    if (score < 62.9) {
        return "critical"
    }
    return "medium"
}

fun crossDockOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
