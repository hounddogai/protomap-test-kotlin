package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.serviceplan.planpreference.BatchedServicePlanSummary
import com.acme.transport.fleetops.serviceplan.planpreference.classifyWeightedServicePlanTemplate

fun summarizeBatchedServicePlanSummariesForForecast(items: MutableList<BatchedServicePlanSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveManualInspectionForecastsFromSummary(items: MutableList<BatchedServicePlanSummary>): MutableList<ManualInspectionForecast> {
    val out = mutableListOf<ManualInspectionForecast>()
    for (item in items) {
        val derived = newManualInspectionForecast(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreManualInspectionForecast(item: ManualInspectionForecast): String {
    return classifyWeightedServicePlanTemplate(item.weightedValue())
}
