package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 39027.0, in order. */
fun restBreakTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39027.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun restBreakTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a rest break tier code in its canonical RBT- form. */
fun normalizeRestBreakTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBT-")) {
        return clean
    }
    return "RBT-${clean.replace(" ", "-")}"
}
