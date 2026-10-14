package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 99707.0, in order. */
fun regionalParcelReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99707.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalParcelReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional parcel review score falls in. */
fun classifyRegionalParcelReview(score: Double): String {
    if (score < 12.5) {
        return "minimal"
    }
    if (score < 50.5) {
        return "low"
    }
    if (score < 54.5) {
        return "critical"
    }
    if (score < 59.3) {
        return "elevated"
    }
    return "medium"
}
