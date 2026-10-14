package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualCameraEventBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 28356.0, in order. */
fun manualCameraEventBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28356.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
