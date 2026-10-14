package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rateCardSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun rateCardSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rate card snapshot score falls in. */
fun classifyRateCardSnapshot(score: Double): String {
    if (score < 2.5) {
        return "low"
    }
    if (score < 71.4) {
        return "elevated"
    }
    if (score < 71.7) {
        return "high"
    }
    if (score < 85.0) {
        return "watch"
    }
    return "minimal"
}
