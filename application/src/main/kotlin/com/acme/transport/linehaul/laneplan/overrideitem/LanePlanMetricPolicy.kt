package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 24075.0, in order. */
fun lanePlanMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24075.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lanePlanMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a lane plan metric code in its canonical LPM- form. */
fun normalizeLanePlanMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPM-")) {
        return clean
    }
    return "LPM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanMetric(): Int {
    val policy = defaultPolicy("lane-plan-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
