package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDwellMetric(): Int {
    val policy = defaultPolicy("adaptive-dwell-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 72798.0, in order. */
fun adaptiveDwellMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72798.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive dwell metric code in its canonical ADM- form. */
fun normalizeAdaptiveDwellMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADM-")) {
        return clean
    }
    return "ADM-${clean.replace(" ", "-")}"
}
