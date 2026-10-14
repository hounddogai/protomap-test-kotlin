package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.safety.scorecard.assignment.PendingScorecardAssignment
import com.acme.transport.safety.scorecard.assignment.classifyFallbackScorecardTarget

fun summarizePendingScorecardAssignmentsForTarget(items: MutableList<PendingScorecardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLenientIncidentTargetsFromAssignment(items: MutableList<PendingScorecardAssignment>): MutableList<LenientIncidentTarget> {
    val out = mutableListOf<LenientIncidentTarget>()
    for (item in items) {
        val derived = newLenientIncidentTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientIncidentTarget(item: LenientIncidentTarget): String {
    return classifyFallbackScorecardTarget(item.effectiveVolume())
}
