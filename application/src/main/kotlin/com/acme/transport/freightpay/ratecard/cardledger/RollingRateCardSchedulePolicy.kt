package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingRateCardScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun rollingRateCardScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
