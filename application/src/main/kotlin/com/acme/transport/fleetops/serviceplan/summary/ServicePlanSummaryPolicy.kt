package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanSummary(): Int {
    val policy = defaultPolicy("service-plan-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun servicePlanSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a service plan summary score falls in. */
fun classifyServicePlanSummary(score: Double): String {
    if (score < 80.1) {
        return "watch"
    }
    if (score < 97.4) {
        return "low"
    }
    return "ok"
}

fun mergeServicePlanSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
