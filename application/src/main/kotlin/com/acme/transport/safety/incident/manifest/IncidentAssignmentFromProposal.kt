package com.acme.transport.safety.incident.manifest

import com.acme.transport.safety.violation.ledger.ViolationProposal
import com.acme.transport.safety.violation.ledger.violationProposalBucket

fun summarizeViolationProposalsForAssignment(items: MutableList<ViolationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveIncidentAssignmentsFromProposal(items: MutableList<ViolationProposal>): MutableList<IncidentAssignment> {
    val out = mutableListOf<IncidentAssignment>()
    for (item in items) {
        val derived = newIncidentAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateIncidentAssignment(item: IncidentAssignment): String = violationProposalBucket(item.netVolume())
