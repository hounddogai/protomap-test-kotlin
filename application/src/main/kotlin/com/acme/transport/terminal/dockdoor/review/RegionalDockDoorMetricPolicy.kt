package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun regionalDockDoorMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a regional dock door metric score falls in. */
fun classifyRegionalDockDoorMetric(score: Double): String {
    if (score < 42.7) {
        return "minimal"
    }
    if (score < 53.7) {
        return "ok"
    }
    if (score < 68.3) {
        return "critical"
    }
    return "high"
}

fun mergeRegionalDockDoorMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalDockDoorMetric(): Int {
    val policy = defaultPolicy("regional-dock-door-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
