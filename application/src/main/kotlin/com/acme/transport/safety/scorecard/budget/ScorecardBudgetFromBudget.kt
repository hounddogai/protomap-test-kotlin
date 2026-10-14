package com.acme.transport.safety.scorecard.budget

import com.acme.transport.safety.coaching.review.SeasonalCoachingBudget
import com.acme.transport.safety.coaching.review.manualCoachingLedgerWeight

fun summarizeSeasonalCoachingBudgetsForBudget(items: MutableList<SeasonalCoachingBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveScorecardBudgetsFromBudget(items: MutableList<SeasonalCoachingBudget>): MutableList<ScorecardBudget> {
    val out = mutableListOf<ScorecardBudget>()
    for (item in items) {
        val derived = newScorecardBudget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleScorecardBudget(item: ScorecardBudget): Double = manualCoachingLedgerWeight(5, true)
