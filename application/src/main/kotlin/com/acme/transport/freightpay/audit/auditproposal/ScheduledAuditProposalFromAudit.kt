package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.dispute.adjustment.DisputeAudit
import com.acme.transport.freightpay.dispute.adjustment.classifyDisputeClaim

fun summarizeDisputeAuditsForProposal(items: MutableList<DisputeAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveScheduledAuditProposalsFromAudit(items: MutableList<DisputeAudit>): MutableList<ScheduledAuditProposal> {
    val out = mutableListOf<ScheduledAuditProposal>()
    for (item in items) {
        val derived = newScheduledAuditProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledAuditProposal(item: ScheduledAuditProposal): String = classifyDisputeClaim(item.netLoad())
