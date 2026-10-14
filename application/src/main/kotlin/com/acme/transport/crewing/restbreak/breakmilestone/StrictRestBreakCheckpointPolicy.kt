package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict rest break checkpoint code in its canonical SRBC- form. */
fun normalizeStrictRestBreakCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBC-")) {
        return clean
    }
    return "SRBC-${clean.replace(" ", "-")}"
}

fun strictRestBreakCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun strictRestBreakCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
