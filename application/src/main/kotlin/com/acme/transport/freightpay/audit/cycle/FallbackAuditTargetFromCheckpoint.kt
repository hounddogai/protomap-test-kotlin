package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.dispute.checkpoint.DisputeCheckpoint
import com.acme.transport.freightpay.dispute.checkpoint.classifyPrimaryDisputeMetric

fun summarizeDisputeCheckpointsForTarget(items: MutableList<DisputeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFallbackAuditTargetsFromCheckpoint(items: MutableList<DisputeCheckpoint>): MutableList<FallbackAuditTarget> {
    val out = mutableListOf<FallbackAuditTarget>()
    for (item in items) {
        val derived = newFallbackAuditTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateFallbackAuditTarget(item: FallbackAuditTarget): String {
    return classifyPrimaryDisputeMetric(item.effectiveValue())
}
