package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRollingViolationWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling violation window score falls in. */
fun classifyRollingViolationWindow(score: Double): String {
    if (score < 53.4) {
        return "elevated"
    }
    if (score < 55.9) {
        return "watch"
    }
    if (score < 61.6) {
        return "medium"
    }
    if (score < 93.6) {
        return "critical"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingViolationWindow(): Int {
    val policy = defaultPolicy("rolling-violation-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingViolationWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
