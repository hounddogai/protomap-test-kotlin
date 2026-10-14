package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.carbonreport.budget.RegionalCarbonReportRequest
import com.acme.transport.emissions.carbonreport.budget.tieredCarbonReportBudgetBucket

fun summarizeRegionalCarbonReportRequestsForCheckpoint(items: MutableList<RegionalCarbonReportRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackIdlingCheckpointsFromRequest(items: MutableList<RegionalCarbonReportRequest>): MutableList<FallbackIdlingCheckpoint> {
    val out = mutableListOf<FallbackIdlingCheckpoint>()
    for (item in items) {
        val derived = newFallbackIdlingCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackIdlingCheckpoint(item: FallbackIdlingCheckpoint): String {
    return tieredCarbonReportBudgetBucket(item.adjustedValue())
}
