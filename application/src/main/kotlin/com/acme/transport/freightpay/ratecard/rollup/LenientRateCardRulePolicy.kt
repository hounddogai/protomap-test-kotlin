package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lenientRateCardRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientRateCardRuleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
