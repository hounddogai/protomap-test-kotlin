package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedAvailabilityDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted availability digest score falls in. */
fun classifyWeightedAvailabilityDigest(score: Double): String {
    if (score < 61.1) {
        return "low"
    }
    if (score < 88.8) {
        return "minimal"
    }
    return "medium"
}

fun weightedAvailabilityDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun weightedAvailabilityDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
