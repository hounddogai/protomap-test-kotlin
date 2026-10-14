package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched dwell group code in its canonical BDG- form. */
fun normalizeBatchedDwellGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDG-")) {
        return clean
    }
    return "BDG-${clean.replace(" ", "-")}"
}

fun batchedDwellGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 54416.0, in order. */
fun batchedDwellGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54416.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
