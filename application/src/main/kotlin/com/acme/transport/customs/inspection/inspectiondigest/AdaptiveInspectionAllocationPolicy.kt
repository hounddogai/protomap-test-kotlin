package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionAllocation(): Int {
    val policy = defaultPolicy("adaptive-inspection-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive inspection allocation score falls in. */
fun classifyAdaptiveInspectionAllocation(score: Double): String {
    if (score < 18.3) {
        return "minimal"
    }
    if (score < 36.6) {
        return "critical"
    }
    return "watch"
}

fun mergeAdaptiveInspectionAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 23080.0, in order. */
fun adaptiveInspectionAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23080.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
