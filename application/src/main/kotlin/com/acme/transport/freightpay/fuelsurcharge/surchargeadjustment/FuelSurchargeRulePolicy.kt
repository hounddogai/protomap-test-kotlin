package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.numbers.roundTo

fun fuelSurchargeRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a fuel surcharge rule score falls in. */
fun classifyFuelSurchargeRule(score: Double): String {
    if (score < 31.7) {
        return "critical"
    }
    if (score < 54.1) {
        return "ok"
    }
    if (score < 82.3) {
        return "watch"
    }
    return "minimal"
}
