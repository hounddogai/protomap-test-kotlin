package com.acme.transport.safety.scorecard.budget

import com.acme.transport.safety.incident.incidentsnapshot.IncidentNotice
import com.acme.transport.safety.incident.incidentsnapshot.classifyPendingIncidentRequest

fun summarizeIncidentNoticesForManifest(items: MutableList<IncidentNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveScorecardManifestsFromNotice(items: MutableList<IncidentNotice>): MutableList<ScorecardManifest> {
    val out = mutableListOf<ScorecardManifest>()
    for (item in items) {
        val derived = newScorecardManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchScorecardManifest(item: ScorecardManifest): String = classifyPendingIncidentRequest(item.weightedTotal())
