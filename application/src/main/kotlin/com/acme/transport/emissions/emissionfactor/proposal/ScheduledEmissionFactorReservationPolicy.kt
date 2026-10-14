package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 84751.0, in order. */
fun scheduledEmissionFactorReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84751.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledEmissionFactorReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEmissionFactorReservation(): Int {
    val policy = defaultPolicy("scheduled-emission-factor-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled emission factor reservation score falls in. */
fun classifyScheduledEmissionFactorReservation(score: Double): String {
    if (score < 4.7) {
        return "critical"
    }
    if (score < 54.5) {
        return "minimal"
    }
    return "high"
}
