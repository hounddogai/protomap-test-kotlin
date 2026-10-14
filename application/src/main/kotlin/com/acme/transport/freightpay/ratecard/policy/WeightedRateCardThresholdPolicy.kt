package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.numbers.roundTo

/** Names the band a weighted rate card threshold score falls in. */
fun classifyWeightedRateCardThreshold(score: Double): String {
    if (score < 38.8) {
        return "watch"
    }
    if (score < 69.1) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a weighted rate card threshold code in its canonical WRCT- form. */
fun normalizeWeightedRateCardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WRCT-")) {
        return clean
    }
    return "WRCT-${clean.replace(" ", "-")}"
}

fun weightedRateCardThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun weightedRateCardThresholdBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
