package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientUtilizationPlan(): Int {
    val policy = defaultPolicy("lenient-utilization-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 29625.0, in order. */
fun lenientUtilizationPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29625.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient utilization plan score falls in. */
fun classifyLenientUtilizationPlan(score: Double): String {
    if (score < 10.4) {
        return "high"
    }
    if (score < 26.9) {
        return "low"
    }
    if (score < 75.4) {
        return "elevated"
    }
    if (score < 93.7) {
        return "critical"
    }
    return "ok"
}

fun mergeLenientUtilizationPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
