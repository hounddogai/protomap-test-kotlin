package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.defect.summary.DefectSummary
import com.acme.transport.fleetops.defect.summary.normalizeDefectEntryCode

fun summarizeDefectSummariesForProposal(items: MutableList<DefectSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryLeaseReturnProposalsFromSummary(items: MutableList<DefectSummary>): MutableList<PrimaryLeaseReturnProposal> {
    val out = mutableListOf<PrimaryLeaseReturnProposal>()
    for (item in items) {
        val derived = newPrimaryLeaseReturnProposal(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scorePrimaryLeaseReturnProposal(item: PrimaryLeaseReturnProposal): String {
    return normalizeDefectEntryCode(item.code)
}
