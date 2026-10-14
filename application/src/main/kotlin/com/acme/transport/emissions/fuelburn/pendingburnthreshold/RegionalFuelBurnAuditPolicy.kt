package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.numbers.roundTo

fun regionalFuelBurnAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a regional fuel burn audit score falls in. */
fun classifyRegionalFuelBurnAudit(score: Double): String {
    if (score < 16.4) {
        return "medium"
    }
    if (score < 30.9) {
        return "high"
    }
    return "minimal"
}

fun regionalFuelBurnAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
