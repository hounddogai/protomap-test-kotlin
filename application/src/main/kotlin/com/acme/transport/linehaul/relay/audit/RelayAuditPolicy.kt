package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun relayAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a relay audit score falls in. */
fun classifyRelayAudit(score: Double): String {
    if (score < 23.5) {
        return "ok"
    }
    if (score < 46.5) {
        return "minimal"
    }
    if (score < 72.7) {
        return "high"
    }
    if (score < 89.4) {
        return "watch"
    }
    return "critical"
}

fun mergeRelayAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
