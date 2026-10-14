package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.numbers.roundTo

/** Puts a weighted trailer yard entry code in its canonical WTYE- form. */
fun normalizeWeightedTrailerYardEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTYE-")) {
        return clean
    }
    return "WTYE-${clean.replace(" ", "-")}"
}

fun weightedTrailerYardEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
