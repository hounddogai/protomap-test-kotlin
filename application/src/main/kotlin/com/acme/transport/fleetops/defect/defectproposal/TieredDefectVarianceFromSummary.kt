package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.fleetops.serviceplan.planpreference.BatchedServicePlanSummary
import com.acme.transport.fleetops.serviceplan.planpreference.classifyWeightedServicePlanTemplate

fun summarizeBatchedServicePlanSummariesForVariance(items: MutableList<BatchedServicePlanSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveTieredDefectVariancesFromSummary(items: MutableList<BatchedServicePlanSummary>): MutableList<TieredDefectVariance> {
    val out = mutableListOf<TieredDefectVariance>()
    for (item in items) {
        val derived = newTieredDefectVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveTieredDefectVariance(item: TieredDefectVariance): String {
    return classifyWeightedServicePlanTemplate(item.adjustedExposure())
}
