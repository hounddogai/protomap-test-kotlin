package com.acme.transport.safety.violation.threshold

import com.acme.transport.safety.incident.proposal.IncidentTier
import com.acme.transport.safety.incident.proposal.classifyBatchedIncidentProposal

fun summarizeIncidentTiersForThreshold(items: MutableList<IncidentTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveViolationThresholdsFromTier(items: MutableList<IncidentTier>): MutableList<ViolationThreshold> {
    val out = mutableListOf<ViolationThreshold>()
    for (item in items) {
        val derived = newViolationThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyViolationThreshold(item: ViolationThreshold): String {
    return classifyBatchedIncidentProposal(item.effectiveVolume())
}
