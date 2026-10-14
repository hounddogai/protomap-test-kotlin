package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun fuelSurchargeOverrideBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun fuelSurchargeOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
