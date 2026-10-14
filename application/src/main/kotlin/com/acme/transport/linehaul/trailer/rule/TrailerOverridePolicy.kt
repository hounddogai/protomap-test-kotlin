package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.numbers.roundTo

/** Puts a trailer override code in its canonical TO- form. */
fun normalizeTrailerOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TO-")) {
        return clean
    }
    return "TO-${clean.replace(" ", "-")}"
}

fun trailerOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
