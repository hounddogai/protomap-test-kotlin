package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveAuditRecord(): Int {
    val policy = defaultPolicy("adaptive-audit-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive audit record score falls in. */
fun classifyAdaptiveAuditRecord(score: Double): String {
    if (score < 53.2) {
        return "medium"
    }
    if (score < 77.2) {
        return "critical"
    }
    if (score < 82.7) {
        return "high"
    }
    if (score < 85.5) {
        return "elevated"
    }
    return "ok"
}

fun adaptiveAuditRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun mergeAdaptiveAuditRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
