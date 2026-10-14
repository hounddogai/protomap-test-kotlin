package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched fuel burn forecast score falls in. */
fun classifyBatchedFuelBurnForecast(score: Double): String {
    if (score < 83.4) {
        return "minimal"
    }
    if (score < 84.9) {
        return "medium"
    }
    if (score < 93.9) {
        return "critical"
    }
    return "high"
}

fun batchedFuelBurnForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
