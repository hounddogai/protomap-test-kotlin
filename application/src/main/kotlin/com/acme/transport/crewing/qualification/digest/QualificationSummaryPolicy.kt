package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 30733.0, in order. */
fun qualificationSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30733.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationSummary(): Int {
    val policy = defaultPolicy("qualification-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a qualification summary score falls in. */
fun classifyQualificationSummary(score: Double): String {
    if (score < 10.6) {
        return "critical"
    }
    if (score < 35.2) {
        return "minimal"
    }
    if (score < 42.9) {
        return "elevated"
    }
    if (score < 92.3) {
        return "watch"
    }
    return "ok"
}
