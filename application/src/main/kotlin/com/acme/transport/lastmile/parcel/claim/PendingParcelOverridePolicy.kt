package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 77700.0, in order. */
fun pendingParcelOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77700.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingParcelOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun pendingParcelOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending parcel override score falls in. */
fun classifyPendingParcelOverride(score: Double): String {
    if (score < 62.7) {
        return "high"
    }
    if (score < 78.2) {
        return "ok"
    }
    return "medium"
}
