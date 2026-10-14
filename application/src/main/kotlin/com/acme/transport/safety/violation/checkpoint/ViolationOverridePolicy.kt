package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 61417.0, in order. */
fun violationOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61417.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a violation override score falls in. */
fun classifyViolationOverride(score: Double): String {
    if (score < 55.6) {
        return "ok"
    }
    if (score < 76.7) {
        return "minimal"
    }
    if (score < 81.3) {
        return "critical"
    }
    if (score < 81.6) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationOverride(): Int {
    val policy = defaultPolicy("violation-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun violationOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
