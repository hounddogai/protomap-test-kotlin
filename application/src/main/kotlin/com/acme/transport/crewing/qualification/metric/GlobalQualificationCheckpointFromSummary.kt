package com.acme.transport.crewing.qualification.metric

import com.acme.transport.crewing.restbreak.summary.RollingRestBreakSummary
import com.acme.transport.crewing.restbreak.summary.restBreakTemplateWeight

fun summarizeRollingRestBreakSummariesForCheckpoint(items: MutableList<RollingRestBreakSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveGlobalQualificationCheckpointsFromSummary(items: MutableList<RollingRestBreakSummary>): MutableList<GlobalQualificationCheckpoint> {
    val out = mutableListOf<GlobalQualificationCheckpoint>()
    for (item in items) {
        val derived = newGlobalQualificationCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitGlobalQualificationCheckpoint(item: GlobalQualificationCheckpoint): Double {
    return restBreakTemplateWeight(8, true)
}
