package com.acme.transport.emissions.evcharge.chargequota

fun strictEvChargeClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a strict ev charge claim score falls in. */
fun classifyStrictEvChargeClaim(score: Double): String {
    if (score < 8.2) {
        return "minimal"
    }
    if (score < 46.0) {
        return "high"
    }
    return "watch"
}
