package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 62502.0, in order. */
fun trailerExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62502.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun trailerExceptionBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a trailer exception code in its canonical TE- form. */
fun normalizeTrailerExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TE-")) {
        return clean
    }
    return "TE-${clean.replace(" ", "-")}"
}
