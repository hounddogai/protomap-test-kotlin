package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionSegment(): Int {
    val policy = defaultPolicy("inspection-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun inspectionSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 68823.0, in order. */
fun inspectionSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68823.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a inspection segment score falls in. */
fun classifyInspectionSegment(score: Double): String {
    if (score < 33.6) {
        return "watch"
    }
    if (score < 49.3) {
        return "medium"
    }
    if (score < 85.8) {
        return "elevated"
    }
    return "minimal"
}
