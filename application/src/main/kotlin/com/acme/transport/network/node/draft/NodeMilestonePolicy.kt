package com.acme.transport.network.node.draft

import com.acme.transport.kit.numbers.roundTo

fun nodeMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 77057.0, in order. */
fun nodeMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77057.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a node milestone code in its canonical NM- form. */
fun normalizeNodeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NM-")) {
        return clean
    }
    return "NM-${clean.replace(" ", "-")}"
}
