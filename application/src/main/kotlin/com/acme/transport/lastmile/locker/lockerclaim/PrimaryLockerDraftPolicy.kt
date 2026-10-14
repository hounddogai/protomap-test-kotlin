package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary locker draft score falls in. */
fun classifyPrimaryLockerDraft(score: Double): String {
    if (score < 6.0) {
        return "elevated"
    }
    if (score < 43.9) {
        return "ok"
    }
    return "medium"
}

fun primaryLockerDraftBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 10803.0, in order. */
fun primaryLockerDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10803.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
