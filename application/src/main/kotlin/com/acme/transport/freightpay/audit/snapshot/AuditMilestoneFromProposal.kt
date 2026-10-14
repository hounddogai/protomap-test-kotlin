package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.freightpay.audit.auditproposal.ScheduledAuditProposal
import com.acme.transport.freightpay.audit.auditproposal.auditManifestBucket

fun summarizeScheduledAuditProposalsForMilestone(items: MutableList<ScheduledAuditProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAuditMilestonesFromProposal(items: MutableList<ScheduledAuditProposal>): MutableList<AuditMilestone> {
    val out = mutableListOf<AuditMilestone>()
    for (item in items) {
        val derived = newAuditMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankAuditMilestone(item: AuditMilestone): String = auditManifestBucket(item.weightedLoad())
