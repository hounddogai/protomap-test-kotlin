package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tender baseline score falls in. */
fun classifyTenderBaseline(score: Double): String {
    if (score < 10.8) {
        return "critical"
    }
    if (score < 38.8) {
        return "medium"
    }
    if (score < 66.8) {
        return "minimal"
    }
    return "low"
}

fun tenderBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
