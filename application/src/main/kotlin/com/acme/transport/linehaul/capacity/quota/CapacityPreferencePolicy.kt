package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun capacityPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityPreference(): Int {
    val policy = defaultPolicy("capacity-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeCapacityPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capacity preference score falls in. */
fun classifyCapacityPreference(score: Double): String {
    if (score < 1.2) {
        return "elevated"
    }
    if (score < 6.7) {
        return "watch"
    }
    if (score < 35.0) {
        return "high"
    }
    if (score < 67.2) {
        return "medium"
    }
    return "ok"
}
