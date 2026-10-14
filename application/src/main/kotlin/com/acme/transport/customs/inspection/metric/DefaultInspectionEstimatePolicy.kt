package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultInspectionEstimate(): Int {
    val policy = defaultPolicy("default-inspection-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultInspectionEstimateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 4644.0, in order. */
fun defaultInspectionEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4644.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default inspection estimate score falls in. */
fun classifyDefaultInspectionEstimate(score: Double): String {
    if (score < 20.5) {
        return "elevated"
    }
    if (score < 33.6) {
        return "minimal"
    }
    return "watch"
}
