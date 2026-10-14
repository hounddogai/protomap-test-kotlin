package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 37351.0, in order. */
fun legacyNodeMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37351.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy node metric code in its canonical LNM- form. */
fun normalizeLegacyNodeMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNM-")) {
        return clean
    }
    return "LNM-${clean.replace(" ", "-")}"
}

fun legacyNodeMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun legacyNodeMetricBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
