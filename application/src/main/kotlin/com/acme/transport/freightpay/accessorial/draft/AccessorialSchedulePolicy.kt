package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.numbers.roundTo

fun accessorialScheduleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 75970.0, in order. */
fun accessorialScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75970.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a accessorial schedule score falls in. */
fun classifyAccessorialSchedule(score: Double): String {
    if (score < 2.8) {
        return "critical"
    }
    if (score < 14.1) {
        return "medium"
    }
    if (score < 77.1) {
        return "ok"
    }
    return "high"
}
