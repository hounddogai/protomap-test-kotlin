package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.numbers.roundTo

fun rollingCapacityRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 92737.0, in order. */
fun rollingCapacityRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92737.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling capacity rollup code in its canonical RCR- form. */
fun normalizeRollingCapacityRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}
