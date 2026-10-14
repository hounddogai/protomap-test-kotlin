package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.scorecard.budget.ScorecardManifest
import com.acme.transport.safety.scorecard.budget.normalizeScorecardBudgetCode

fun summarizeScorecardManifestsForAudit(items: MutableList<ScorecardManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveManualScorecardAuditsFromManifest(items: MutableList<ScorecardManifest>): MutableList<ManualScorecardAudit> {
    val out = mutableListOf<ManualScorecardAudit>()
    for (item in items) {
        val derived = newManualScorecardAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchManualScorecardAudit(item: ManualScorecardAudit): String = normalizeScorecardBudgetCode(item.code)
