package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun clearanceDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a clearance digest score falls in. */
fun classifyClearanceDigest(score: Double): String {
    if (score < 7.9) {
        return "ok"
    }
    if (score < 82.5) {
        return "elevated"
    }
    if (score < 82.9) {
        return "critical"
    }
    if (score < 94.6) {
        return "low"
    }
    return "watch"
}

fun clearanceDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun clearanceDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
