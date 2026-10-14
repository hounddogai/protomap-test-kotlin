package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a hub template score falls in. */
fun classifyHubTemplate(score: Double): String {
    if (score < 38.9) {
        return "elevated"
    }
    if (score < 58.8) {
        return "watch"
    }
    if (score < 68.4) {
        return "medium"
    }
    if (score < 78.2) {
        return "critical"
    }
    return "ok"
}

fun hubTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun hubTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
