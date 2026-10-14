package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun rollingDwellPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeRollingDwellPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingDwellPlan(): Int {
    val policy = defaultPolicy("rolling-dwell-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling dwell plan score falls in. */
fun classifyRollingDwellPlan(score: Double): String {
    if (score < 62.2) {
        return "minimal"
    }
    if (score < 65.1) {
        return "medium"
    }
    if (score < 86.0) {
        return "high"
    }
    return "elevated"
}
