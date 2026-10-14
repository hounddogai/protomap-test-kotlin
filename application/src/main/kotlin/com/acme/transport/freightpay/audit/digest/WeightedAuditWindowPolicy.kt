package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeWeightedAuditWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a weighted audit window score falls in. */
fun classifyWeightedAuditWindow(score: Double): String {
    if (score < 27.8) {
        return "critical"
    }
    if (score < 68.7) {
        return "elevated"
    }
    return "high"
}

fun weightedAuditWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun weightedAuditWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
