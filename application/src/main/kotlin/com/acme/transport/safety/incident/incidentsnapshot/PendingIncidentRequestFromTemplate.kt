package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.safety.scorecard.profile.ManualScorecardTemplate
import com.acme.transport.safety.scorecard.profile.classifyScorecardDraft

fun summarizeManualScorecardTemplatesForRequest(items: MutableList<ManualScorecardTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingIncidentRequestsFromTemplate(items: MutableList<ManualScorecardTemplate>): MutableList<PendingIncidentRequest> {
    val out = mutableListOf<PendingIncidentRequest>()
    for (item in items) {
        val derived = newPendingIncidentRequest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergePendingIncidentRequest(item: PendingIncidentRequest): String {
    return classifyScorecardDraft(item.measuredValue())
}
