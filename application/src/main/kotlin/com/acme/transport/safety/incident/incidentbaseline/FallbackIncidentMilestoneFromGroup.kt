package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.safety.scorecard.entry.ManualScorecardGroup
import com.acme.transport.safety.scorecard.entry.scorecardCycleBucket

fun summarizeManualScorecardGroupsForMilestone(items: MutableList<ManualScorecardGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveFallbackIncidentMilestonesFromGroup(items: MutableList<ManualScorecardGroup>): MutableList<FallbackIncidentMilestone> {
    val out = mutableListOf<FallbackIncidentMilestone>()
    for (item in items) {
        val derived = newFallbackIncidentMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankFallbackIncidentMilestone(item: FallbackIncidentMilestone): String {
    return scorecardCycleBucket(item.adjustedTotal())
}
