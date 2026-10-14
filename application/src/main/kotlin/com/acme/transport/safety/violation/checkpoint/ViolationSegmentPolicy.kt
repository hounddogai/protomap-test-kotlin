package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeViolationSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a violation segment score falls in. */
fun classifyViolationSegment(score: Double): String {
    if (score < 58.7) {
        return "high"
    }
    if (score < 66.9) {
        return "ok"
    }
    if (score < 72.1) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationSegment(): Int {
    val policy = defaultPolicy("violation-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
