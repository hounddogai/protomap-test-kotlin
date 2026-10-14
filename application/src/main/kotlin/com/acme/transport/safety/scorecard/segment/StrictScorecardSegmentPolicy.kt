package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictScorecardSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictScorecardSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
