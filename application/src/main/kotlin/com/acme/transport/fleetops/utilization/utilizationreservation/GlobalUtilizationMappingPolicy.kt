package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.numbers.roundTo

/** Names the band a global utilization mapping score falls in. */
fun classifyGlobalUtilizationMapping(score: Double): String {
    if (score < 21.9) {
        return "watch"
    }
    if (score < 49.2) {
        return "elevated"
    }
    if (score < 60.8) {
        return "medium"
    }
    if (score < 91.0) {
        return "low"
    }
    return "high"
}

fun globalUtilizationMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
