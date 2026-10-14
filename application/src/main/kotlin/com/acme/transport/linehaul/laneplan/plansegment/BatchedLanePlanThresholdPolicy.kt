package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedLanePlanThreshold(): Int {
    val policy = defaultPolicy("batched-lane-plan-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeBatchedLanePlanThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a batched lane plan threshold score falls in. */
fun classifyBatchedLanePlanThreshold(score: Double): String {
    if (score < 2.9) {
        return "ok"
    }
    if (score < 26.6) {
        return "elevated"
    }
    if (score < 60.1) {
        return "critical"
    }
    if (score < 88.8) {
        return "minimal"
    }
    return "low"
}
