package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryIncidentQuota(): Int {
    val policy = defaultPolicy("primary-incident-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary incident quota score falls in. */
fun classifyPrimaryIncidentQuota(score: Double): String {
    if (score < 1.0) {
        return "minimal"
    }
    if (score < 11.1) {
        return "critical"
    }
    if (score < 41.8) {
        return "watch"
    }
    if (score < 56.3) {
        return "low"
    }
    return "ok"
}

fun primaryIncidentQuotaBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 20761.0, in order. */
fun primaryIncidentQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20761.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
