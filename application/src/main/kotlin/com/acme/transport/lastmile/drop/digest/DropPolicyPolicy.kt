package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeDropPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropPolicy(): Int {
    val policy = defaultPolicy("drop-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a drop policy score falls in. */
fun classifyDropPolicy(score: Double): String {
    if (score < 6.1) {
        return "elevated"
    }
    if (score < 43.5) {
        return "high"
    }
    if (score < 95.1) {
        return "low"
    }
    return "critical"
}
