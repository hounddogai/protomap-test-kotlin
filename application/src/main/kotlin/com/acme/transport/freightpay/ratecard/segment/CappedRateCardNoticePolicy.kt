package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedRateCardNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 68963.0, in order. */
fun cappedRateCardNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68963.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedRateCardNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
