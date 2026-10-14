package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 29083.0, in order. */
fun fallbackDwellMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29083.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback dwell milestone code in its canonical FDM- form. */
fun normalizeFallbackDwellMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDM-")) {
        return clean
    }
    return "FDM-${clean.replace(" ", "-")}"
}

fun fallbackDwellMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun fallbackDwellMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
