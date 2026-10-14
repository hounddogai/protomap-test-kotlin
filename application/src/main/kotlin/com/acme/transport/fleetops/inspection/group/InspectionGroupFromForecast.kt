package com.acme.transport.fleetops.inspection.group

import com.acme.transport.fleetops.inspection.digest.InspectionForecast
import com.acme.transport.fleetops.inspection.digest.inspectionDigestBucket

fun summarizeInspectionForecastsForGroup(items: MutableList<InspectionForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveInspectionGroupsFromForecast(items: MutableList<InspectionForecast>): MutableList<InspectionGroup> {
    val out = mutableListOf<InspectionGroup>()
    for (item in items) {
        val derived = newInspectionGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveInspectionGroup(item: InspectionGroup): String = inspectionDigestBucket(item.effectiveLoad())
