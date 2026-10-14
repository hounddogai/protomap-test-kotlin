package com.acme.transport.fleetops.serviceplan.threshold

/** Names the band a strict service plan claim score falls in. */
fun classifyStrictServicePlanClaim(score: Double): String {
    if (score < 8.7) {
        return "elevated"
    }
    if (score < 71.6) {
        return "critical"
    }
    return "high"
}

fun strictServicePlanClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
