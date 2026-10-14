package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.safety.incident.defaultincidentplan.IncidentMapping
import com.acme.transport.safety.incident.defaultincidentplan.classifyLenientIncidentAssignment

fun summarizeIncidentMappingsForProposal(items: MutableList<IncidentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveStrictNearMissProposalsFromMapping(items: MutableList<IncidentMapping>): MutableList<StrictNearMissProposal> {
    val out = mutableListOf<StrictNearMissProposal>()
    for (item in items) {
        val derived = newStrictNearMissProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreStrictNearMissProposal(item: StrictNearMissProposal): String {
    return classifyLenientIncidentAssignment(item.weightedLoad())
}
