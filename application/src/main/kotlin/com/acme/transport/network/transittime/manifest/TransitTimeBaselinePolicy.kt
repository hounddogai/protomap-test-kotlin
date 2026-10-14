package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.numbers.roundTo

fun transitTimeBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a transit time baseline code in its canonical TTB- form. */
fun normalizeTransitTimeBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTB-")) {
        return clean
    }
    return "TTB-${clean.replace(" ", "-")}"
}

fun transitTimeBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 71477.0, in order. */
fun transitTimeBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71477.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
