package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.numbers.roundTo

fun adaptiveUtilizationRollupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a adaptive utilization rollup score falls in. */
fun classifyAdaptiveUtilizationRollup(score: Double): String {
    if (score < 18.4) {
        return "elevated"
    }
    if (score < 62.5) {
        return "high"
    }
    return "watch"
}
