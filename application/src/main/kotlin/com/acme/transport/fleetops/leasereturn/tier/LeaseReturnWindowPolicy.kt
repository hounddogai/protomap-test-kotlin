package com.acme.transport.fleetops.leasereturn.tier

/** Names the band a lease return window score falls in. */
fun classifyLeaseReturnWindow(score: Double): String {
    if (score < 2.3) {
        return "ok"
    }
    if (score < 3.8) {
        return "low"
    }
    if (score < 65.0) {
        return "elevated"
    }
    if (score < 71.6) {
        return "high"
    }
    return "watch"
}

fun leaseReturnWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
