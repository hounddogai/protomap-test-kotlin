package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun attemptExceptionBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeAttemptExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a attempt exception score falls in. */
fun classifyAttemptException(score: Double): String {
    if (score < 7.2) {
        return "critical"
    }
    if (score < 8.6) {
        return "high"
    }
    if (score < 38.5) {
        return "medium"
    }
    return "elevated"
}
