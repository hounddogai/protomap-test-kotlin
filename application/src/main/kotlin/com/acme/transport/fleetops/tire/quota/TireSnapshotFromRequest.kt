package com.acme.transport.fleetops.tire.quota

import com.acme.transport.fleetops.inspection.mapping.InspectionRequest
import com.acme.transport.fleetops.inspection.mapping.inspectionOverrideWeight

fun summarizeInspectionRequestsForSnapshot(items: MutableList<InspectionRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTireSnapshotsFromRequest(items: MutableList<InspectionRequest>): MutableList<TireSnapshot> {
    val out = mutableListOf<TireSnapshot>()
    for (item in items) {
        val derived = newTireSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTireSnapshot(item: TireSnapshot): Double = inspectionOverrideWeight(2, true)
