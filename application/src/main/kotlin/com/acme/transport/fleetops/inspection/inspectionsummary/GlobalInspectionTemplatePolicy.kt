package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalInspectionTemplate(): Int {
    val policy = defaultPolicy("global-inspection-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 58531.0, in order. */
fun globalInspectionTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58531.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global inspection template score falls in. */
fun classifyGlobalInspectionTemplate(score: Double): String {
    if (score < 34.4) {
        return "high"
    }
    if (score < 45.6) {
        return "elevated"
    }
    if (score < 84.2) {
        return "low"
    }
    if (score < 87.5) {
        return "watch"
    }
    return "minimal"
}

fun globalInspectionTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
