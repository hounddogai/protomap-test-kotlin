package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.numbers.roundTo

fun dropGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 97595.0, in order. */
fun dropGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97595.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a drop group code in its canonical DG- form. */
fun normalizeDropGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DG-")) {
        return clean
    }
    return "DG-${clean.replace(" ", "-")}"
}

fun dropGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
