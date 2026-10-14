package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual route offset audit score falls in. */
fun classifyManualRouteOffsetAudit(score: Double): String {
    if (score < 22.6) {
        return "critical"
    }
    if (score < 40.4) {
        return "minimal"
    }
    return "high"
}

fun manualRouteOffsetAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
