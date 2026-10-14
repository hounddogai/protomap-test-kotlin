package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a regional ev charge template score falls in. */
fun classifyRegionalEvChargeTemplate(score: Double): String {
    if (score < 11.4) {
        return "high"
    }
    if (score < 67.0) {
        return "critical"
    }
    return "watch"
}

fun regionalEvChargeTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 95785.0, in order. */
fun regionalEvChargeTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95785.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalEvChargeTemplate(): Int {
    val policy = defaultPolicy("regional-ev-charge-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
