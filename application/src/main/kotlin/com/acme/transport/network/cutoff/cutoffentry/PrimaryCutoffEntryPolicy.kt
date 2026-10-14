package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryCutoffEntryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun primaryCutoffEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a primary cutoff entry score falls in. */
fun classifyPrimaryCutoffEntry(score: Double): String {
    if (score < 17.7) {
        return "critical"
    }
    if (score < 33.1) {
        return "watch"
    }
    if (score < 87.2) {
        return "minimal"
    }
    if (score < 97.3) {
        return "ok"
    }
    return "low"
}

fun primaryCutoffEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
