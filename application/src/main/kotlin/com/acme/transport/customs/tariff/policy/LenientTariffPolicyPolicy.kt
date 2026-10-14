package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 2240.0, in order. */
fun lenientTariffPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientTariffPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
