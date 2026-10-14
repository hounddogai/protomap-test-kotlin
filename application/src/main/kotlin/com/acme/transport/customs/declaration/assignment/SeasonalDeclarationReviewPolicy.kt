package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.numbers.roundTo

fun seasonalDeclarationReviewBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a seasonal declaration review code in its canonical SDR- form. */
fun normalizeSeasonalDeclarationReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDR-")) {
        return clean
    }
    return "SDR-${clean.replace(" ", "-")}"
}
