package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 84903.0, in order. */
fun fuelSurchargePreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84903.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelSurchargePreferenceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a fuel surcharge preference score falls in. */
fun classifyFuelSurchargePreference(score: Double): String {
    if (score < 28.8) {
        return "high"
    }
    if (score < 80.5) {
        return "medium"
    }
    if (score < 96.1) {
        return "watch"
    }
    if (score < 97.7) {
        return "critical"
    }
    return "ok"
}
