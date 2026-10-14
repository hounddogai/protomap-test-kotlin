package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.safety.nearmiss.rollingmissrecord.AdaptiveNearMissException
import com.acme.transport.safety.nearmiss.rollingmissrecord.normalizeGlobalNearMissRecordCode

fun summarizeAdaptiveNearMissExceptionsForBudget(items: MutableList<AdaptiveNearMissException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyScorecardBudgetsFromException(items: MutableList<AdaptiveNearMissException>): MutableList<LegacyScorecardBudget> {
    val out = mutableListOf<LegacyScorecardBudget>()
    for (item in items) {
        val derived = newLegacyScorecardBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectLegacyScorecardBudget(item: LegacyScorecardBudget): String {
    return normalizeGlobalNearMissRecordCode(item.code)
}
