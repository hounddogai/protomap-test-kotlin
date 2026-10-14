package com.acme.transport.safety.scorecard.profile

import com.acme.transport.safety.incident.slot.BatchedIncidentOverride
import com.acme.transport.safety.incident.slot.cappedIncidentSlotBucket

fun summarizeBatchedIncidentOverridesForMapping(items: MutableList<BatchedIncidentOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledScorecardMappingsFromOverride(items: MutableList<BatchedIncidentOverride>): MutableList<ScheduledScorecardMapping> {
    val out = mutableListOf<ScheduledScorecardMapping>()
    for (item in items) {
        val derived = newScheduledScorecardMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyScheduledScorecardMapping(item: ScheduledScorecardMapping): String {
    return cappedIncidentSlotBucket(item.adjustedLoad())
}
