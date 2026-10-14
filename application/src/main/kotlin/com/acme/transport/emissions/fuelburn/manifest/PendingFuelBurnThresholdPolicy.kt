package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.numbers.roundTo

fun pendingFuelBurnThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a pending fuel burn threshold score falls in. */
fun classifyPendingFuelBurnThreshold(score: Double): String {
    if (score < 25.9) {
        return "elevated"
    }
    if (score < 30.9) {
        return "critical"
    }
    return "medium"
}
