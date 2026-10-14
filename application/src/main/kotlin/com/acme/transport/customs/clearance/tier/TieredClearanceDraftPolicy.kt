package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tiered clearance draft score falls in. */
fun classifyTieredClearanceDraft(score: Double): String {
    if (score < 19.1) {
        return "medium"
    }
    if (score < 53.1) {
        return "minimal"
    }
    if (score < 91.5) {
        return "ok"
    }
    return "low"
}

fun tieredClearanceDraftBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
