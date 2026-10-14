package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.numbers.roundTo

fun pendingTrailerProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a pending trailer profile score falls in. */
fun classifyPendingTrailerProfile(score: Double): String {
    if (score < 3.8) {
        return "critical"
    }
    if (score < 37.4) {
        return "medium"
    }
    if (score < 63.8) {
        return "high"
    }
    return "watch"
}

/** Puts a pending trailer profile code in its canonical PTP- form. */
fun normalizePendingTrailerProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTP-")) {
        return clean
    }
    return "PTP-${clean.replace(" ", "-")}"
}

fun pendingTrailerProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
