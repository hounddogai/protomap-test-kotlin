package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.incident.incidentmanifest.IncidentDraft
import com.acme.transport.safety.incident.incidentmanifest.incidentVarianceBucket

fun summarizeIncidentDraftsForQuota(items: MutableList<IncidentDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackScorecardQuotasFromDraft(items: MutableList<IncidentDraft>): MutableList<FallbackScorecardQuota> {
    val out = mutableListOf<FallbackScorecardQuota>()
    for (item in items) {
        val derived = newFallbackScorecardQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitFallbackScorecardQuota(item: FallbackScorecardQuota): String {
    return incidentVarianceBucket(item.adjustedExposure())
}
