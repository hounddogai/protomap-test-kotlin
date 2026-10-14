package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.numbers.roundTo

fun manualDeclarationAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a manual declaration adjustment score falls in. */
fun classifyManualDeclarationAdjustment(score: Double): String {
    if (score < 12.1) {
        return "high"
    }
    if (score < 24.3) {
        return "low"
    }
    if (score < 64.3) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 52185.0, in order. */
fun manualDeclarationAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52185.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
