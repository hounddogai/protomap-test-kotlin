package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rate card template score falls in. */
fun classifyRateCardTemplate(score: Double): String {
    if (score < 34.7) {
        return "high"
    }
    if (score < 40.4) {
        return "minimal"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 2046.0, in order. */
fun rateCardTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2046.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rateCardTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
