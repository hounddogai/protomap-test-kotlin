package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.numbers.roundTo

fun batchedRateCardMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun batchedRateCardMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 88798.0, in order. */
fun batchedRateCardMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88798.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
