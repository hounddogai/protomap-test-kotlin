package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardMilestone(): Int {
    val policy = defaultPolicy("trailer-yard-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a trailer yard milestone score falls in. */
fun classifyTrailerYardMilestone(score: Double): String {
    if (score < 17.5) {
        return "watch"
    }
    if (score < 24.3) {
        return "low"
    }
    if (score < 95.8) {
        return "medium"
    }
    if (score < 97.3) {
        return "high"
    }
    return "critical"
}

fun mergeTrailerYardMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
