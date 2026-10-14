package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeLenientDwellPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientDwellPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a lenient dwell policy score falls in. */
fun classifyLenientDwellPolicy(score: Double): String {
    if (score < 2.2) {
        return "watch"
    }
    if (score < 52.5) {
        return "medium"
    }
    if (score < 55.3) {
        return "critical"
    }
    if (score < 73.1) {
        return "elevated"
    }
    return "low"
}

fun lenientDwellPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
