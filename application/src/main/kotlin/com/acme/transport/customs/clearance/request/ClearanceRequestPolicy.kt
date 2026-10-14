package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.numbers.roundTo

/** Puts a clearance request code in its canonical CR- form. */
fun normalizeClearanceRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CR-")) {
        return clean
    }
    return "CR-${clean.replace(" ", "-")}"
}

fun clearanceRequestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 2646.0, in order. */
fun clearanceRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2646.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
