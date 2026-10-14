package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 62675.0, in order. */
fun strictRateCardPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62675.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictRateCardPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeStrictRateCardPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
