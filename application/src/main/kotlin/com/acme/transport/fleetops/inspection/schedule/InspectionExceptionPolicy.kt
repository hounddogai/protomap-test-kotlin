package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun inspectionExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeInspectionExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection exception score falls in. */
fun classifyInspectionException(score: Double): String {
    if (score < 19.1) {
        return "medium"
    }
    if (score < 40.8) {
        return "watch"
    }
    if (score < 67.2) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionException(): Int {
    val policy = defaultPolicy("inspection-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
