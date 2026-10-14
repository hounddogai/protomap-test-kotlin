package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 73333.0, in order. */
fun seasonalServiceAreaAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73333.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a seasonal service area allocation score falls in. */
fun classifySeasonalServiceAreaAllocation(score: Double): String {
    if (score < 13.2) {
        return "high"
    }
    if (score < 24.8) {
        return "medium"
    }
    if (score < 56.2) {
        return "critical"
    }
    if (score < 84.3) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalServiceAreaAllocation(): Int {
    val policy = defaultPolicy("seasonal-service-area-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalServiceAreaAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
