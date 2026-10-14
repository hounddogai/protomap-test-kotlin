package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.numbers.roundTo

fun fallbackTrailerYardVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a fallback trailer yard variance code in its canonical FTYV- form. */
fun normalizeFallbackTrailerYardVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTYV-")) {
        return clean
    }
    return "FTYV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 99074.0, in order. */
fun fallbackTrailerYardVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99074.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackTrailerYardVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
