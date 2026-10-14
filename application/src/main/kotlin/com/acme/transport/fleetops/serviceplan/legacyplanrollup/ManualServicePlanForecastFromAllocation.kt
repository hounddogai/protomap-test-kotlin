package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.fleetops.inspection.reservation.FallbackInspectionAllocation
import com.acme.transport.fleetops.inspection.reservation.lenientInspectionSegmentBucket

fun summarizeFallbackInspectionAllocationsForForecast(items: MutableList<FallbackInspectionAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualServicePlanForecastsFromAllocation(items: MutableList<FallbackInspectionAllocation>): MutableList<ManualServicePlanForecast> {
    val out = mutableListOf<ManualServicePlanForecast>()
    for (item in items) {
        val derived = newManualServicePlanForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitManualServicePlanForecast(item: ManualServicePlanForecast): String {
    return lenientInspectionSegmentBucket(item.measuredValue())
}
