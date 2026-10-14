package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.numbers.roundTo

fun lenientTariffEntryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 94653.0, in order. */
fun lenientTariffEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94653.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
