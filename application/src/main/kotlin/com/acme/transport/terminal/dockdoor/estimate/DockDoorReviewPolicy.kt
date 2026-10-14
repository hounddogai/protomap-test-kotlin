package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 14655.0, in order. */
fun dockDoorReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14655.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorReview(): Int {
    val policy = defaultPolicy("dock-door-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a dock door review score falls in. */
fun classifyDockDoorReview(score: Double): String {
    if (score < 21.0) {
        return "low"
    }
    if (score < 55.2) {
        return "minimal"
    }
    if (score < 89.2) {
        return "elevated"
    }
    return "critical"
}

fun dockDoorReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
