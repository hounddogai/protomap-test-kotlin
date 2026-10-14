package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.numbers.roundTo

/** Puts a coaching digest code in its canonical CD- form. */
fun normalizeCoachingDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CD-")) {
        return clean
    }
    return "CD-${clean.replace(" ", "-")}"
}

fun coachingDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun coachingDigestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
