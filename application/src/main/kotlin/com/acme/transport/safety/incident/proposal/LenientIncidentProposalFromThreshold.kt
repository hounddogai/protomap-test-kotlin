package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.scorecard.threshold.ScorecardThreshold
import com.acme.transport.safety.scorecard.threshold.normalizeScorecardMappingCode

fun summarizeScorecardThresholdsForProposal(items: MutableList<ScorecardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLenientIncidentProposalsFromThreshold(items: MutableList<ScorecardThreshold>): MutableList<LenientIncidentProposal> {
    val out = mutableListOf<LenientIncidentProposal>()
    for (item in items) {
        val derived = newLenientIncidentProposal(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLenientIncidentProposal(item: LenientIncidentProposal): String {
    return normalizeScorecardMappingCode(item.code)
}
