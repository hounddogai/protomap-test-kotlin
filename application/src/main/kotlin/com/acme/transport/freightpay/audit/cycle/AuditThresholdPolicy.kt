package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeAuditThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a audit threshold score falls in. */
fun classifyAuditThreshold(score: Double): String {
    if (score < 26.5) {
        return "low"
    }
    if (score < 27.0) {
        return "medium"
    }
    if (score < 29.9) {
        return "minimal"
    }
    if (score < 34.7) {
        return "high"
    }
    return "watch"
}

fun auditThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
