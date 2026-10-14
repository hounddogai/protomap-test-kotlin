package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 23617.0, in order. */
fun lenientZoneNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23617.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient zone notice score falls in. */
fun classifyLenientZoneNotice(score: Double): String {
    if (score < 64.0) {
        return "critical"
    }
    if (score < 67.8) {
        return "watch"
    }
    if (score < 79.7) {
        return "low"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientZoneNotice(): Int {
    val policy = defaultPolicy("lenient-zone-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
