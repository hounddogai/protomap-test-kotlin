package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.safety.scorecard.scorecardgroup.AdaptiveScorecardGroup
import com.acme.transport.safety.scorecard.scorecardgroup.adaptiveScorecardSegmentBucket

fun summarizeAdaptiveScorecardGroupsForCycle(items: MutableList<AdaptiveScorecardGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictNearMissCyclesFromGroup(items: MutableList<AdaptiveScorecardGroup>): MutableList<StrictNearMissCycle> {
    val out = mutableListOf<StrictNearMissCycle>()
    for (item in items) {
        val derived = newStrictNearMissCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankStrictNearMissCycle(item: StrictNearMissCycle): String {
    return adaptiveScorecardSegmentBucket(item.measuredValue())
}
