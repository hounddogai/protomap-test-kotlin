package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.safety.scorecard.budget.ScorecardManifest
import com.acme.transport.safety.scorecard.budget.classifyScorecardBudget

fun summarizeScorecardManifestsForBudget(items: MutableList<ScorecardManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveIncidentBudgetsFromManifest(items: MutableList<ScorecardManifest>): MutableList<IncidentBudget> {
    val out = mutableListOf<IncidentBudget>()
    for (item in items) {
        val derived = newIncidentBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveIncidentBudget(item: IncidentBudget): String = classifyScorecardBudget(item.effectiveTotal())
