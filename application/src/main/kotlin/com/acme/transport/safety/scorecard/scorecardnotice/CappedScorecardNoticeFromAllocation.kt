package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.safety.violation.allocation.SeasonalViolationAllocation
import com.acme.transport.safety.violation.allocation.seasonalViolationAllocationBucket

fun summarizeSeasonalViolationAllocationsForNotice(items: MutableList<SeasonalViolationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedScorecardNoticesFromAllocation(items: MutableList<SeasonalViolationAllocation>): MutableList<CappedScorecardNotice> {
    val out = mutableListOf<CappedScorecardNotice>()
    for (item in items) {
        val derived = newCappedScorecardNotice(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCappedScorecardNotice(item: CappedScorecardNotice): String {
    return seasonalViolationAllocationBucket(item.weightedLoad())
}
