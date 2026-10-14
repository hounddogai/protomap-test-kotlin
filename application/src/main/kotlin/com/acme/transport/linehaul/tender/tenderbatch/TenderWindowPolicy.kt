package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.numbers.roundTo

fun tenderWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a tender window code in its canonical TW- form. */
fun normalizeTenderWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TW-")) {
        return clean
    }
    return "TW-${clean.replace(" ", "-")}"
}

fun tenderWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a tender window score falls in. */
fun classifyTenderWindow(score: Double): String {
    if (score < 35.6) {
        return "elevated"
    }
    if (score < 43.3) {
        return "high"
    }
    if (score < 68.1) {
        return "low"
    }
    return "medium"
}
