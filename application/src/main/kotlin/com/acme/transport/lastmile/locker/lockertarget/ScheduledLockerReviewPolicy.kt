package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun scheduledLockerReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 45752.0, in order. */
fun scheduledLockerReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45752.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledLockerReviewBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a scheduled locker review score falls in. */
fun classifyScheduledLockerReview(score: Double): String {
    if (score < 11.1) {
        return "ok"
    }
    if (score < 17.0) {
        return "elevated"
    }
    if (score < 42.6) {
        return "high"
    }
    if (score < 89.6) {
        return "minimal"
    }
    return "medium"
}
