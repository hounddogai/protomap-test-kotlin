package com.acme.transport.safety.scorecard.slot

import com.acme.transport.safety.incident.proposal.LenientIncidentProposal
import com.acme.transport.safety.incident.proposal.classifyBatchedIncidentProposal

fun summarizeLenientIncidentProposalsForAssignment(items: MutableList<LenientIncidentProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveScorecardAssignmentsFromProposal(items: MutableList<LenientIncidentProposal>): MutableList<ScorecardAssignment> {
    val out = mutableListOf<ScorecardAssignment>()
    for (item in items) {
        val derived = newScorecardAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleScorecardAssignment(item: ScorecardAssignment): String {
    return classifyBatchedIncidentProposal(item.netVolume())
}
