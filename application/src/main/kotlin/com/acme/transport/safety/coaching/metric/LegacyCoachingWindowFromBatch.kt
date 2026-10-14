package com.acme.transport.safety.coaching.metric

import com.acme.transport.safety.violation.summary.GlobalViolationBatch
import com.acme.transport.safety.violation.summary.classifyGlobalViolationBatch

fun summarizeGlobalViolationBatchesForWindow(items: MutableList<GlobalViolationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyCoachingWindowsFromBatch(items: MutableList<GlobalViolationBatch>): MutableList<LegacyCoachingWindow> {
    val out = mutableListOf<LegacyCoachingWindow>()
    for (item in items) {
        val derived = newLegacyCoachingWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLegacyCoachingWindow(item: LegacyCoachingWindow): String {
    return classifyGlobalViolationBatch(item.weightedVolume())
}
