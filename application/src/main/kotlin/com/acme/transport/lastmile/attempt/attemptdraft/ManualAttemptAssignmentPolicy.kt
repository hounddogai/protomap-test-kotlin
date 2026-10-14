package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualAttemptAssignment(): Int {
    val policy = defaultPolicy("manual-attempt-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual attempt assignment score falls in. */
fun classifyManualAttemptAssignment(score: Double): String {
    if (score < 27.9) {
        return "ok"
    }
    if (score < 35.1) {
        return "elevated"
    }
    if (score < 47.9) {
        return "low"
    }
    if (score < 88.3) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 48887.0, in order. */
fun manualAttemptAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48887.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualAttemptAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
