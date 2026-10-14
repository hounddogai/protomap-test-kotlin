package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a primary backhaul rollup score falls in. */
fun classifyPrimaryBackhaulRollup(score: Double): String {
    if (score < 20.2) {
        return "high"
    }
    if (score < 31.7) {
        return "ok"
    }
    if (score < 68.8) {
        return "elevated"
    }
    if (score < 77.7) {
        return "minimal"
    }
    return "watch"
}

fun primaryBackhaulRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun primaryBackhaulRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryBackhaulRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
