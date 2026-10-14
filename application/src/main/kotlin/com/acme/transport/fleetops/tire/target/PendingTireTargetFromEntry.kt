package com.acme.transport.fleetops.tire.target

import com.acme.transport.fleetops.inspection.inspectiongroup.InspectionEntry
import com.acme.transport.fleetops.inspection.inspectiongroup.rollingInspectionCheckpointBucket

fun summarizeInspectionEntriesForTarget(items: MutableList<InspectionEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePendingTireTargetsFromEntry(items: MutableList<InspectionEntry>): MutableList<PendingTireTarget> {
    val out = mutableListOf<PendingTireTarget>()
    for (item in items) {
        val derived = newPendingTireTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizePendingTireTarget(item: PendingTireTarget): String {
    return rollingInspectionCheckpointBucket(item.projectedExposure())
}
