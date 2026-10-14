package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun transitTimeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 93162.0, in order. */
fun transitTimeManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93162.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
