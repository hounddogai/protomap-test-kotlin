package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanBundle(): Int {
    val policy = defaultPolicy("lane-plan-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lane plan bundle score falls in. */
fun classifyLanePlanBundle(score: Double): String {
    if (score < 22.2) {
        return "minimal"
    }
    if (score < 30.1) {
        return "low"
    }
    if (score < 84.6) {
        return "critical"
    }
    if (score < 97.6) {
        return "elevated"
    }
    return "medium"
}

fun lanePlanBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 13467.0, in order. */
fun lanePlanBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13467.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
