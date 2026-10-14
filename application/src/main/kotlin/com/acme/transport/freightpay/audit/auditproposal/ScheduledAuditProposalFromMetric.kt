package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.audit.proposal.AuditMetric
import com.acme.transport.freightpay.audit.proposal.normalizeAuditAdjustmentCode

fun summarizeAuditMetricsForProposal(items: MutableList<AuditMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveScheduledAuditProposalsFromMetric(items: MutableList<AuditMetric>): MutableList<ScheduledAuditProposal> {
    val out = mutableListOf<ScheduledAuditProposal>()
    for (item in items) {
        val derived = newScheduledAuditProposal(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeScheduledAuditProposal(item: ScheduledAuditProposal): String {
    return normalizeAuditAdjustmentCode(item.code)
}
