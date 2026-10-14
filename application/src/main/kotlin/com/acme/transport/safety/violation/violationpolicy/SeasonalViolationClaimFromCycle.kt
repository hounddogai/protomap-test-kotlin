package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.safety.scorecard.cycle.LegacyScorecardCycle
import com.acme.transport.safety.scorecard.cycle.classifyScorecardMetric

fun summarizeLegacyScorecardCyclesForClaim(items: MutableList<LegacyScorecardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveSeasonalViolationClaimsFromCycle(items: MutableList<LegacyScorecardCycle>): MutableList<SeasonalViolationClaim> {
    val out = mutableListOf<SeasonalViolationClaim>()
    for (item in items) {
        val derived = newSeasonalViolationClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalViolationClaim(item: SeasonalViolationClaim): String {
    return classifyScorecardMetric(item.adjustedLoad())
}
