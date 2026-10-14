package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.freightpay.audit.auditproposal.ScheduledAuditProposal
import com.acme.transport.freightpay.audit.auditproposal.classifyAuditManifest

fun summarizeScheduledAuditProposalsForSummary(items: MutableList<ScheduledAuditProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveDisputeSummariesFromProposal(items: MutableList<ScheduledAuditProposal>): MutableList<DisputeSummary> {
    val out = mutableListOf<DisputeSummary>()
    for (item in items) {
        val derived = newDisputeSummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreDisputeSummary(item: DisputeSummary): String = classifyAuditManifest(item.effectiveLoad())
