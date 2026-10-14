package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultRestBreakEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun defaultRestBreakEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default rest break entry score falls in. */
fun classifyDefaultRestBreakEntry(score: Double): String {
    if (score < 10.2) {
        return "minimal"
    }
    if (score < 63.6) {
        return "high"
    }
    if (score < 72.2) {
        return "medium"
    }
    return "elevated"
}
