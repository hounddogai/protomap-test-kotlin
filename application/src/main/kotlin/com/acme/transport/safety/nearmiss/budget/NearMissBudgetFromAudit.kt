package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.safety.scorecard.proposal.ScorecardAudit
import com.acme.transport.safety.scorecard.proposal.scorecardSegmentWeight

fun summarizeScorecardAuditsForBudget(items: MutableList<ScorecardAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveNearMissBudgetsFromAudit(items: MutableList<ScorecardAudit>): MutableList<NearMissBudget> {
    val out = mutableListOf<NearMissBudget>()
    for (item in items) {
        val derived = newNearMissBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveNearMissBudget(item: NearMissBudget): Double = scorecardSegmentWeight(5, true)
