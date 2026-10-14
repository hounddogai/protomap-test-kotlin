package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.numbers.roundTo

fun tieredFuelSurchargeAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun tieredFuelSurchargeAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
