package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeNearMissExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a near miss exception score falls in. */
fun classifyNearMissException(score: Double): String {
    if (score < 21.2) {
        return "critical"
    }
    if (score < 72.3) {
        return "watch"
    }
    if (score < 97.1) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNearMissException(): Int {
    val policy = defaultPolicy("near-miss-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
