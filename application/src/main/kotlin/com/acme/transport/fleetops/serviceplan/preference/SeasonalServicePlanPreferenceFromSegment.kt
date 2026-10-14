package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.fleetops.defect.metric.DefectSegment
import com.acme.transport.fleetops.defect.metric.pendingDefectMetricWeight

fun summarizeDefectSegmentsForPreference(items: MutableList<DefectSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveSeasonalServicePlanPreferencesFromSegment(items: MutableList<DefectSegment>): MutableList<SeasonalServicePlanPreference> {
    val out = mutableListOf<SeasonalServicePlanPreference>()
    for (item in items) {
        val derived = newSeasonalServicePlanPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankSeasonalServicePlanPreference(item: SeasonalServicePlanPreference): Double {
    return pendingDefectMetricWeight(5, true)
}
