package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.numbers.roundTo

fun manualViolationSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a manual violation summary score falls in. */
fun classifyManualViolationSummary(score: Double): String {
    if (score < 5.2) {
        return "high"
    }
    if (score < 72.3) {
        return "minimal"
    }
    if (score < 91.8) {
        return "critical"
    }
    if (score < 99.1) {
        return "ok"
    }
    return "low"
}
