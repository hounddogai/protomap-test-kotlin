package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.safety.incident.group.IncidentBatch
import com.acme.transport.safety.incident.group.classifyIncidentGroup

fun summarizeIncidentBatchesForBundle(items: MutableList<IncidentBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualScorecardBundlesFromBatch(items: MutableList<IncidentBatch>): MutableList<ManualScorecardBundle> {
    val out = mutableListOf<ManualScorecardBundle>()
    for (item in items) {
        val derived = newManualScorecardBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveManualScorecardBundle(item: ManualScorecardBundle): String {
    return classifyIncidentGroup(item.adjustedVolume())
}
