package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fuelSurchargeRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 71439.0, in order. */
fun fuelSurchargeRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71439.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
