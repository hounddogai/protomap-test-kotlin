package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.numbers.roundTo

/** Names the band a defect tier score falls in. */
fun classifyDefectTier(score: Double): String {
    if (score < 33.1) {
        return "minimal"
    }
    if (score < 58.0) {
        return "medium"
    }
    if (score < 62.4) {
        return "ok"
    }
    if (score < 78.4) {
        return "critical"
    }
    return "watch"
}

fun defectTierBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
