package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun trailerEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun trailerEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a trailer entry score falls in. */
fun classifyTrailerEntry(score: Double): String {
    if (score < 16.9) {
        return "elevated"
    }
    if (score < 29.8) {
        return "critical"
    }
    if (score < 66.0) {
        return "medium"
    }
    if (score < 85.1) {
        return "watch"
    }
    return "ok"
}
