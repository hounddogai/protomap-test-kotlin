package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun accessorialReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a accessorial review score falls in. */
fun classifyAccessorialReview(score: Double): String {
    if (score < 32.3) {
        return "ok"
    }
    if (score < 33.4) {
        return "medium"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 19804.0, in order. */
fun accessorialReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19804.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
