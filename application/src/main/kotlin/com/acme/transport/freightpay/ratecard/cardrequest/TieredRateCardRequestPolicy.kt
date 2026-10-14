package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.numbers.roundTo

fun tieredRateCardRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun tieredRateCardRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a tiered rate card request score falls in. */
fun classifyTieredRateCardRequest(score: Double): String {
    if (score < 29.1) {
        return "medium"
    }
    if (score < 40.7) {
        return "watch"
    }
    if (score < 46.2) {
        return "ok"
    }
    if (score < 64.1) {
        return "critical"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 82516.0, in order. */
fun tieredRateCardRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
