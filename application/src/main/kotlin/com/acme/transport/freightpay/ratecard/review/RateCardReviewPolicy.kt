package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rate card review score falls in. */
fun classifyRateCardReview(score: Double): String {
    if (score < 40.3) {
        return "elevated"
    }
    if (score < 90.0) {
        return "critical"
    }
    if (score < 93.0) {
        return "minimal"
    }
    return "low"
}

fun rateCardReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a rate card review code in its canonical RCR- form. */
fun normalizeRateCardReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}

fun rateCardReviewBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
