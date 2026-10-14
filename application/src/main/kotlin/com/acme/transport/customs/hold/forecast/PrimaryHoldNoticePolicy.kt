package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.numbers.roundTo

fun primaryHoldNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a primary hold notice score falls in. */
fun classifyPrimaryHoldNotice(score: Double): String {
    if (score < 16.9) {
        return "high"
    }
    if (score < 24.1) {
        return "watch"
    }
    if (score < 25.1) {
        return "elevated"
    }
    if (score < 26.7) {
        return "medium"
    }
    return "low"
}

/** Puts a primary hold notice code in its canonical PHN- form. */
fun normalizePrimaryHoldNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PHN-")) {
        return clean
    }
    return "PHN-${clean.replace(" ", "-")}"
}
