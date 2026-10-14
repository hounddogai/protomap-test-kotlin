package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a cutoff assignment score falls in. */
fun classifyCutoffAssignment(score: Double): String {
    if (score < 33.6) {
        return "critical"
    }
    if (score < 84.2) {
        return "minimal"
    }
    if (score < 95.4) {
        return "watch"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffAssignment(): Int {
    val policy = defaultPolicy("cutoff-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 53794.0, in order. */
fun cutoffAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53794.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cutoffAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
