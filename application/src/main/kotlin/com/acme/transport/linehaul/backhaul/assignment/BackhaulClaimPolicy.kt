package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.numbers.roundTo

fun backhaulClaimBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 32235.0, in order. */
fun backhaulClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32235.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a backhaul claim code in its canonical BC- form. */
fun normalizeBackhaulClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BC-")) {
        return clean
    }
    return "BC-${clean.replace(" ", "-")}"
}
