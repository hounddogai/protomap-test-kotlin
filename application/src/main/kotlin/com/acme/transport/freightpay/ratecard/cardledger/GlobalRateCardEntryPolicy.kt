package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.numbers.roundTo

/** Puts a global rate card entry code in its canonical GRCE- form. */
fun normalizeGlobalRateCardEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GRCE-")) {
        return clean
    }
    return "GRCE-${clean.replace(" ", "-")}"
}

/** Names the band a global rate card entry score falls in. */
fun classifyGlobalRateCardEntry(score: Double): String {
    if (score < 41.1) {
        return "watch"
    }
    if (score < 42.7) {
        return "critical"
    }
    if (score < 81.6) {
        return "low"
    }
    return "medium"
}

fun globalRateCardEntryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
