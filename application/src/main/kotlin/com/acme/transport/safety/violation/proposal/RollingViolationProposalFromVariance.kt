package com.acme.transport.safety.violation.proposal

import com.acme.transport.safety.coaching.coachingsegment.SeasonalCoachingVariance
import com.acme.transport.safety.coaching.coachingsegment.seasonalCoachingVarianceBucket

fun summarizeSeasonalCoachingVariancesForProposal(items: MutableList<SeasonalCoachingVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRollingViolationProposalsFromVariance(items: MutableList<SeasonalCoachingVariance>): MutableList<RollingViolationProposal> {
    val out = mutableListOf<RollingViolationProposal>()
    for (item in items) {
        val derived = newRollingViolationProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRollingViolationProposal(item: RollingViolationProposal): String {
    return seasonalCoachingVarianceBucket(item.effectiveTotal())
}
