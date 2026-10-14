package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 31285.0, in order. */
fun pendingQualificationPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31285.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending qualification preference score falls in. */
fun classifyPendingQualificationPreference(score: Double): String {
    if (score < 3.4) {
        return "critical"
    }
    if (score < 39.2) {
        return "watch"
    }
    if (score < 82.5) {
        return "ok"
    }
    if (score < 92.4) {
        return "low"
    }
    return "minimal"
}

fun pendingQualificationPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingQualificationPreference(): Int {
    val policy = defaultPolicy("pending-qualification-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
