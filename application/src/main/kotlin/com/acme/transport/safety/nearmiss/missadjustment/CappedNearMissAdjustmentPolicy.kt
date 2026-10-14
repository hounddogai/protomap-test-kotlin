package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedNearMissAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedNearMissAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a capped near miss adjustment score falls in. */
fun classifyCappedNearMissAdjustment(score: Double): String {
    if (score < 14.5) {
        return "medium"
    }
    if (score < 30.5) {
        return "elevated"
    }
    if (score < 47.3) {
        return "low"
    }
    if (score < 62.3) {
        return "watch"
    }
    return "critical"
}
