package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 72003.0, in order. */
fun tieredCrossDockAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72003.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredCrossDockAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a tiered cross dock adjustment code in its canonical TCDA- form. */
fun normalizeTieredCrossDockAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDA-")) {
        return clean
    }
    return "TCDA-${clean.replace(" ", "-")}"
}
