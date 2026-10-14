package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.linehaul.trailer.trailergroup.RollingTrailerGroup
import com.acme.transport.linehaul.trailer.trailergroup.normalizeRollingTrailerGroupCode

fun summarizeRollingTrailerGroupsForMetric(items: MutableList<RollingTrailerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualTrailerMetricsFromGroup(items: MutableList<RollingTrailerGroup>): MutableList<ManualTrailerMetric> {
    val out = mutableListOf<ManualTrailerMetric>()
    for (item in items) {
        val derived = newManualTrailerMetric(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchManualTrailerMetric(item: ManualTrailerMetric): String = normalizeRollingTrailerGroupCode(item.code)
