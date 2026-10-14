package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun pendingParcelRequestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun pendingParcelRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 61342.0, in order. */
fun pendingParcelRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61342.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending parcel request score falls in. */
fun classifyPendingParcelRequest(score: Double): String {
    if (score < 16.1) {
        return "critical"
    }
    if (score < 51.9) {
        return "ok"
    }
    if (score < 74.9) {
        return "high"
    }
    return "medium"
}
