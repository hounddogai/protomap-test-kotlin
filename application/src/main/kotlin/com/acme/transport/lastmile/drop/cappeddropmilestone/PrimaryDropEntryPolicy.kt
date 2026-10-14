package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryDropEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary drop entry score falls in. */
fun classifyPrimaryDropEntry(score: Double): String {
    if (score < 23.6) {
        return "medium"
    }
    if (score < 83.9) {
        return "high"
    }
    if (score < 94.8) {
        return "ok"
    }
    return "critical"
}

fun primaryDropEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
