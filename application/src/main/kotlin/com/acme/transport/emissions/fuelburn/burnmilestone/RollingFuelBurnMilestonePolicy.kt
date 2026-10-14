package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.numbers.roundTo

fun rollingFuelBurnMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a rolling fuel burn milestone score falls in. */
fun classifyRollingFuelBurnMilestone(score: Double): String {
    if (score < 6.8) {
        return "critical"
    }
    if (score < 34.6) {
        return "elevated"
    }
    return "watch"
}
