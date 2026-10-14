package com.acme.transport.emissions.routeoffset.slot

fun fallbackRouteOffsetAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a fallback route offset allocation score falls in. */
fun classifyFallbackRouteOffsetAllocation(score: Double): String {
    if (score < 25.4) {
        return "high"
    }
    if (score < 27.9) {
        return "critical"
    }
    if (score < 62.4) {
        return "watch"
    }
    return "ok"
}
