package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.fleetops.inspection.overrideitem.AdaptiveInspectionEstimate
import com.acme.transport.fleetops.inspection.overrideitem.classifyInspectionBudget

fun summarizeAdaptiveInspectionEstimatesForPlan(items: MutableList<AdaptiveInspectionEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveUtilizationPlansFromEstimate(items: MutableList<AdaptiveInspectionEstimate>): MutableList<AdaptiveUtilizationPlan> {
    val out = mutableListOf<AdaptiveUtilizationPlan>()
    for (item in items) {
        val derived = newAdaptiveUtilizationPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveUtilizationPlan(item: AdaptiveUtilizationPlan): String {
    return classifyInspectionBudget(item.measuredValue())
}
