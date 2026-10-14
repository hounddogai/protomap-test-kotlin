package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.numbers.roundTo

fun fallbackRouteOffsetScoreBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a fallback route offset score code in its canonical FROS- form. */
fun normalizeFallbackRouteOffsetScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FROS-")) {
        return clean
    }
    return "FROS-${clean.replace(" ", "-")}"
}

fun fallbackRouteOffsetScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 93285.0, in order. */
fun fallbackRouteOffsetScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93285.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
