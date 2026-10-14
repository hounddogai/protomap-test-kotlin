package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergePrimaryNodePolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryNodePolicy(): Int {
    val policy = defaultPolicy("primary-node-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryNodePolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a primary node policy score falls in. */
fun classifyPrimaryNodePolicy(score: Double): String {
    if (score < 8.9) {
        return "watch"
    }
    if (score < 25.8) {
        return "ok"
    }
    if (score < 71.0) {
        return "medium"
    }
    return "low"
}
