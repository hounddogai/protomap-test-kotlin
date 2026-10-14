package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.numbers.roundTo

fun manualAccessorialEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a manual accessorial entry score falls in. */
fun classifyManualAccessorialEntry(score: Double): String {
    if (score < 46.5) {
        return "watch"
    }
    if (score < 73.2) {
        return "low"
    }
    if (score < 90.3) {
        return "elevated"
    }
    return "ok"
}
