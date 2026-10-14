package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy cross dock variance code in its canonical LCDV- form. */
fun normalizeLegacyCrossDockVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCDV-")) {
        return clean
    }
    return "LCDV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 58532.0, in order. */
fun legacyCrossDockVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58532.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyCrossDockVarianceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
