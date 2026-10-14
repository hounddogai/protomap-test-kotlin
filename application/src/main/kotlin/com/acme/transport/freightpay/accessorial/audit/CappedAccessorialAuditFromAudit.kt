package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.freightpay.dispute.disputeassignment.CappedDisputeAudit
import com.acme.transport.freightpay.dispute.disputeassignment.cappedDisputeAuditBucket

fun summarizeCappedDisputeAuditsForAudit(items: MutableList<CappedDisputeAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCappedAccessorialAuditsFromAudit(items: MutableList<CappedDisputeAudit>): MutableList<CappedAccessorialAudit> {
    val out = mutableListOf<CappedAccessorialAudit>()
    for (item in items) {
        val derived = newCappedAccessorialAudit(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveCappedAccessorialAudit(item: CappedAccessorialAudit): String {
    return cappedDisputeAuditBucket(item.adjustedVolume())
}
