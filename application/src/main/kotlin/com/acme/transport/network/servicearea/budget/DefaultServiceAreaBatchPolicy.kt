package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a default service area batch score falls in. */
fun classifyDefaultServiceAreaBatch(score: Double): String {
    if (score < 10.2) {
        return "critical"
    }
    if (score < 52.4) {
        return "elevated"
    }
    if (score < 68.6) {
        return "minimal"
    }
    if (score < 96.1) {
        return "watch"
    }
    return "medium"
}

fun defaultServiceAreaBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun defaultServiceAreaBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
