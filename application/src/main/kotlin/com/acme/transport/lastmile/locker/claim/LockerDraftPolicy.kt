package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lockerDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a locker draft score falls in. */
fun classifyLockerDraft(score: Double): String {
    if (score < 6.5) {
        return "elevated"
    }
    if (score < 56.6) {
        return "medium"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 74058.0, in order. */
fun lockerDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74058.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
