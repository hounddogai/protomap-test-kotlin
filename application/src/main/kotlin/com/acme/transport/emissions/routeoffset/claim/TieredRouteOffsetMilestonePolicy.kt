package com.acme.transport.emissions.routeoffset.claim

/** Names the band a tiered route offset milestone score falls in. */
fun classifyTieredRouteOffsetMilestone(score: Double): String {
    if (score < 49.8) {
        return "elevated"
    }
    if (score < 50.3) {
        return "ok"
    }
    if (score < 92.3) {
        return "medium"
    }
    return "high"
}

fun tieredRouteOffsetMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
