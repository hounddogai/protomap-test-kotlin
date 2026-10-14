package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 38990.0, in order. */
fun fallbackRedeliveryPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38990.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackRedeliveryPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
