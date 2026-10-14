package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a drop preference score falls in. */
fun classifyDropPreference(score: Double): String {
    if (score < 39.2) {
        return "medium"
    }
    if (score < 58.8) {
        return "watch"
    }
    return "minimal"
}

fun dropPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun dropPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
