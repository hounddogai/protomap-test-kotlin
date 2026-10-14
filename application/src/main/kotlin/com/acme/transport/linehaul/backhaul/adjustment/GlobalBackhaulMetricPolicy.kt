package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeGlobalBackhaulMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalBackhaulMetric(): Int {
    val policy = defaultPolicy("global-backhaul-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global backhaul metric score falls in. */
fun classifyGlobalBackhaulMetric(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 3.2) {
        return "watch"
    }
    if (score < 58.4) {
        return "minimal"
    }
    return "critical"
}

fun globalBackhaulMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
