package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.retry.defaultPolicy

fun fallbackBackhaulReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a fallback backhaul reservation score falls in. */
fun classifyFallbackBackhaulReservation(score: Double): String {
    if (score < 1.3) {
        return "high"
    }
    if (score < 53.7) {
        return "medium"
    }
    if (score < 57.4) {
        return "low"
    }
    if (score < 99.0) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackBackhaulReservation(): Int {
    val policy = defaultPolicy("fallback-backhaul-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 40982.0, in order. */
fun fallbackBackhaulReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40982.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
