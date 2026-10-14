package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.numbers.roundTo

/** Names the band a default rate card policy score falls in. */
fun classifyDefaultRateCardPolicy(score: Double): String {
    if (score < 18.7) {
        return "minimal"
    }
    if (score < 32.9) {
        return "watch"
    }
    if (score < 36.3) {
        return "high"
    }
    return "critical"
}

/** Puts a default rate card policy code in its canonical DRCP- form. */
fun normalizeDefaultRateCardPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DRCP-")) {
        return clean
    }
    return "DRCP-${clean.replace(" ", "-")}"
}

fun defaultRateCardPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun defaultRateCardPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
