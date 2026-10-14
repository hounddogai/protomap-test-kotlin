package com.acme.transport.network.hub.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a primary hub target score falls in. */
fun classifyPrimaryHubTarget(score: Double): String {
    if (score < 50.9) {
        return "minimal"
    }
    if (score < 71.2) {
        return "elevated"
    }
    if (score < 81.8) {
        return "low"
    }
    return "high"
}

fun primaryHubTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryHubTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun primaryHubTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
