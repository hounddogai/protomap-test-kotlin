package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.numbers.roundTo

fun fallbackFuelBurnMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a fallback fuel burn milestone score falls in. */
fun classifyFallbackFuelBurnMilestone(score: Double): String {
    if (score < 27.0) {
        return "elevated"
    }
    if (score < 53.6) {
        return "medium"
    }
    return "low"
}
