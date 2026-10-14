package com.acme.transport.customs.hold.group

import com.acme.transport.kit.numbers.roundTo

/** Names the band a hold agreement score falls in. */
fun classifyHoldAgreement(score: Double): String {
    if (score < 64.8) {
        return "elevated"
    }
    if (score < 83.6) {
        return "ok"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 54890.0, in order. */
fun holdAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54890.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun holdAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
