package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a rest break threshold score falls in. */
fun classifyRestBreakThreshold(score: Double): String {
    if (score < 45.2) {
        return "ok"
    }
    if (score < 68.6) {
        return "elevated"
    }
    return "high"
}

fun mergeRestBreakThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakThreshold(): Int {
    val policy = defaultPolicy("rest-break-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
