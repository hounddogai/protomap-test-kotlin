package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.retry.defaultPolicy

fun primaryTransitTimeMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTransitTimeMapping(): Int {
    val policy = defaultPolicy("primary-transit-time-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary transit time mapping score falls in. */
fun classifyPrimaryTransitTimeMapping(score: Double): String {
    if (score < 5.5) {
        return "critical"
    }
    if (score < 84.6) {
        return "elevated"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 9715.0, in order. */
fun primaryTransitTimeMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9715.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
