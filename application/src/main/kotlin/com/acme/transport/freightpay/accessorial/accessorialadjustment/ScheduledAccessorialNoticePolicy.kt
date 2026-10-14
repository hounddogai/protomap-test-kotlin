package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledAccessorialNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledAccessorialNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 5449.0, in order. */
fun scheduledAccessorialNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5449.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
