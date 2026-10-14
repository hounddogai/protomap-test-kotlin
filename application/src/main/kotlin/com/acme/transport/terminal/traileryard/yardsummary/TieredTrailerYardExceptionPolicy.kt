package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.numbers.roundTo

/** Puts a tiered trailer yard exception code in its canonical TTYE- form. */
fun normalizeTieredTrailerYardExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTYE-")) {
        return clean
    }
    return "TTYE-${clean.replace(" ", "-")}"
}

fun tieredTrailerYardExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
