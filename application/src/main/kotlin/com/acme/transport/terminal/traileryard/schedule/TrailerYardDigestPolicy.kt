package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.numbers.roundTo

fun trailerYardDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a trailer yard digest score falls in. */
fun classifyTrailerYardDigest(score: Double): String {
    if (score < 12.7) {
        return "medium"
    }
    if (score < 50.3) {
        return "high"
    }
    if (score < 52.8) {
        return "low"
    }
    return "ok"
}

/** Puts a trailer yard digest code in its canonical TYD- form. */
fun normalizeTrailerYardDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYD-")) {
        return clean
    }
    return "TYD-${clean.replace(" ", "-")}"
}

fun trailerYardDigestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
