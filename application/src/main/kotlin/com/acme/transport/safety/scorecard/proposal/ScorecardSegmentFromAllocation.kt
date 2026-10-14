package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.safety.nearmiss.adjustment.NearMissAllocation
import com.acme.transport.safety.nearmiss.adjustment.nearMissAllocationWeight

fun summarizeNearMissAllocationsForSegment(items: MutableList<NearMissAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScorecardSegmentsFromAllocation(items: MutableList<NearMissAllocation>): MutableList<ScorecardSegment> {
    val out = mutableListOf<ScorecardSegment>()
    for (item in items) {
        val derived = newScorecardSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitScorecardSegment(item: ScorecardSegment): Double = nearMissAllocationWeight(6, true)
