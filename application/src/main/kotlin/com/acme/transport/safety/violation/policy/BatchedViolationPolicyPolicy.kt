package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a batched violation policy score falls in. */
fun classifyBatchedViolationPolicy(score: Double): String {
    if (score < 40.4) {
        return "high"
    }
    if (score < 61.6) {
        return "watch"
    }
    if (score < 75.4) {
        return "low"
    }
    if (score < 99.6) {
        return "ok"
    }
    return "critical"
}

fun batchedViolationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeBatchedViolationPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
