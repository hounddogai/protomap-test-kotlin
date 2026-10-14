package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal scorecard quota score falls in. */
fun classifySeasonalScorecardQuota(score: Double): String {
    if (score < 22.3) {
        return "minimal"
    }
    if (score < 64.9) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 94516.0, in order. */
fun seasonalScorecardQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalScorecardQuota(): Int {
    val policy = defaultPolicy("seasonal-scorecard-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
