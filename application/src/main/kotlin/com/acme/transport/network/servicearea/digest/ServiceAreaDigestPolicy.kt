package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.numbers.roundTo

/** Puts a service area digest code in its canonical SAD- form. */
fun normalizeServiceAreaDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAD-")) {
        return clean
    }
    return "SAD-${clean.replace(" ", "-")}"
}

fun serviceAreaDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun serviceAreaDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
