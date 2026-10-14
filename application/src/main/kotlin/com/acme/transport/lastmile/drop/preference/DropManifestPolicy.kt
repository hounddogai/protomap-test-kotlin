package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.numbers.roundTo

fun dropManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a drop manifest code in its canonical DM- form. */
fun normalizeDropManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DM-")) {
        return clean
    }
    return "DM-${clean.replace(" ", "-")}"
}

fun dropManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
