package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.fleetops.inspection.adjustment.InspectionAdjustment
import com.acme.transport.fleetops.inspection.adjustment.adaptiveInspectionPolicyWeight

fun summarizeInspectionAdjustmentsForForecast(items: MutableList<InspectionAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveScheduledUtilizationForecastsFromAdjustment(items: MutableList<InspectionAdjustment>): MutableList<ScheduledUtilizationForecast> {
    val out = mutableListOf<ScheduledUtilizationForecast>()
    for (item in items) {
        val derived = newScheduledUtilizationForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledUtilizationForecast(item: ScheduledUtilizationForecast): Double {
    return adaptiveInspectionPolicyWeight(1, true)
}
