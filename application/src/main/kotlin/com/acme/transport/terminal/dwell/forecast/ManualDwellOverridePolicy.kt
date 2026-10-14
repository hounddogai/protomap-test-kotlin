package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDwellOverride(): Int {
    val policy = defaultPolicy("manual-dwell-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 89762.0, in order. */
fun manualDwellOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89762.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual dwell override score falls in. */
fun classifyManualDwellOverride(score: Double): String {
    if (score < 50.1) {
        return "critical"
    }
    if (score < 87.0) {
        return "ok"
    }
    if (score < 90.9) {
        return "low"
    }
    return "watch"
}
