package com.acme.transport.safety.violation.preference

import com.acme.transport.safety.scorecard.scorecardbudget.BatchedScorecardBudget
import com.acme.transport.safety.scorecard.scorecardbudget.scheduledScorecardScheduleBucket

fun summarizeBatchedScorecardBudgetsForTier(items: MutableList<BatchedScorecardBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryViolationTiersFromBudget(items: MutableList<BatchedScorecardBudget>): MutableList<PrimaryViolationTier> {
    val out = mutableListOf<PrimaryViolationTier>()
    for (item in items) {
        val derived = newPrimaryViolationTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scorePrimaryViolationTier(item: PrimaryViolationTier): String {
    return scheduledScorecardScheduleBucket(item.weightedValue())
}
