package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.numbers.roundTo

/** Puts a swap score code in its canonical SS- form. */
fun normalizeSwapScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}

fun swapScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
