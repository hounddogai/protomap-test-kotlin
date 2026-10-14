package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.numbers.roundTo

fun strictTrailerYardCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun strictTrailerYardCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a strict trailer yard checkpoint code in its canonical STYC- form. */
fun normalizeStrictTrailerYardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYC-")) {
        return clean
    }
    return "STYC-${clean.replace(" ", "-")}"
}
