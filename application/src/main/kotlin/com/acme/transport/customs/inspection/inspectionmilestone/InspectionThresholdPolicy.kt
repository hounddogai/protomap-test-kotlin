package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionThreshold(): Int {
    val policy = defaultPolicy("inspection-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 82823.0, in order. */
fun inspectionThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82823.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeInspectionThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection threshold score falls in. */
fun classifyInspectionThreshold(score: Double): String {
    if (score < 2.3) {
        return "elevated"
    }
    if (score < 32.8) {
        return "medium"
    }
    if (score < 43.1) {
        return "watch"
    }
    if (score < 53.0) {
        return "low"
    }
    return "high"
}
