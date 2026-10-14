package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.roster.regionalrostersummary.BatchedRosterSummary
import com.acme.transport.crewing.roster.regionalrostersummary.classifyLegacyRosterSchedule

fun summarizeBatchedRosterSummariesForCheckpoint(items: MutableList<BatchedRosterSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRestBreakCheckpointsFromSummary(items: MutableList<BatchedRosterSummary>): MutableList<RestBreakCheckpoint> {
    val out = mutableListOf<RestBreakCheckpoint>()
    for (item in items) {
        val derived = newRestBreakCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRestBreakCheckpoint(item: RestBreakCheckpoint): String {
    return classifyLegacyRosterSchedule(item.projectedVolume())
}
