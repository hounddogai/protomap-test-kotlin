package com.acme.transport.emissions.evcharge.claim

/** Names the band a ev charge claim score falls in. */
fun classifyEvChargeClaim(score: Double): String {
    if (score < 19.6) {
        return "watch"
    }
    if (score < 23.1) {
        return "minimal"
    }
    if (score < 71.1) {
        return "ok"
    }
    return "medium"
}

fun evChargeClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
