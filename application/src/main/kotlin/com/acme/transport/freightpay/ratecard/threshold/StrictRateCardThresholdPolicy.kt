package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.numbers.roundTo

fun strictRateCardThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun strictRateCardThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a strict rate card threshold code in its canonical SRCT- form. */
fun normalizeStrictRateCardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRCT-")) {
        return clean
    }
    return "SRCT-${clean.replace(" ", "-")}"
}
