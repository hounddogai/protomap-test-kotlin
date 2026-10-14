package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.safety.incident.incidentsegment.IncidentRequest
import com.acme.transport.safety.incident.incidentsegment.classifyPrimaryIncidentSegment

fun summarizeIncidentRequestsForBundle(items: MutableList<IncidentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveScorecardBundlesFromRequest(items: MutableList<IncidentRequest>): MutableList<ScorecardBundle> {
    val out = mutableListOf<ScorecardBundle>()
    for (item in items) {
        val derived = newScorecardBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchScorecardBundle(item: ScorecardBundle): String = classifyPrimaryIncidentSegment(item.projectedVolume())
