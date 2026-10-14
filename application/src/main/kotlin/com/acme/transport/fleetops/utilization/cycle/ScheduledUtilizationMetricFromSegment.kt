package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.fleetops.utilization.tieredutilizationplan.LegacyUtilizationSegment
import com.acme.transport.fleetops.utilization.tieredutilizationplan.classifyUtilizationDraft

fun summarizeLegacyUtilizationSegmentsForMetric(items: MutableList<LegacyUtilizationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveScheduledUtilizationMetricsFromSegment(items: MutableList<LegacyUtilizationSegment>): MutableList<ScheduledUtilizationMetric> {
    val out = mutableListOf<ScheduledUtilizationMetric>()
    for (item in items) {
        val derived = newScheduledUtilizationMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeScheduledUtilizationMetric(item: ScheduledUtilizationMetric): String {
    return classifyUtilizationDraft(item.projectedLoad())
}
