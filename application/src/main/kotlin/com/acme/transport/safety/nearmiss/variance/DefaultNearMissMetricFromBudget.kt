package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.safety.scorecard.budget.ScorecardBudget
import com.acme.transport.safety.scorecard.budget.classifyScorecardBudget

fun summarizeScorecardBudgetsForMetric(items: MutableList<ScorecardBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultNearMissMetricsFromBudget(items: MutableList<ScorecardBudget>): MutableList<DefaultNearMissMetric> {
    val out = mutableListOf<DefaultNearMissMetric>()
    for (item in items) {
        val derived = newDefaultNearMissMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultNearMissMetric(item: DefaultNearMissMetric): String {
    return classifyScorecardBudget(item.weightedTotal())
}
