package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a parcel slot score falls in. */
fun classifyParcelSlot(score: Double): String {
    if (score < 13.7) {
        return "ok"
    }
    if (score < 21.3) {
        return "medium"
    }
    if (score < 45.5) {
        return "elevated"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 78373.0, in order. */
fun parcelSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78373.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun parcelSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
