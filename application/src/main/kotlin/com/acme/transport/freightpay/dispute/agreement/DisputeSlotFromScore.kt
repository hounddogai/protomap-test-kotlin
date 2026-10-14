package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.freightpay.audit.claim.PendingAuditScore
import com.acme.transport.freightpay.audit.claim.normalizeAdaptiveAuditBatchCode

fun summarizePendingAuditScoresForSlot(items: MutableList<PendingAuditScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDisputeSlotsFromScore(items: MutableList<PendingAuditScore>): MutableList<DisputeSlot> {
    val out = mutableListOf<DisputeSlot>()
    for (item in items) {
        val derived = newDisputeSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDisputeSlot(item: DisputeSlot): String = normalizeAdaptiveAuditBatchCode(item.code)
