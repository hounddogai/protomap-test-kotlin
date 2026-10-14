package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalServiceAreaAgreementBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 49221.0, in order. */
fun regionalServiceAreaAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49221.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalServiceAreaAgreement(): Int {
    val policy = defaultPolicy("regional-service-area-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional service area agreement score falls in. */
fun classifyRegionalServiceAreaAgreement(score: Double): String {
    if (score < 5.6) {
        return "critical"
    }
    if (score < 28.1) {
        return "minimal"
    }
    if (score < 29.7) {
        return "low"
    }
    if (score < 85.4) {
        return "ok"
    }
    return "watch"
}
