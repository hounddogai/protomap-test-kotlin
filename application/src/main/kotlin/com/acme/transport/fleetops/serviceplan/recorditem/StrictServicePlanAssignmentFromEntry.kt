package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.tire.request.TieredTireEntry
import com.acme.transport.fleetops.tire.request.defaultTireManifestBucket

fun summarizeTieredTireEntriesForAssignment(items: MutableList<TieredTireEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveStrictServicePlanAssignmentsFromEntry(items: MutableList<TieredTireEntry>): MutableList<StrictServicePlanAssignment> {
    val out = mutableListOf<StrictServicePlanAssignment>()
    for (item in items) {
        val derived = newStrictServicePlanAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignStrictServicePlanAssignment(item: StrictServicePlanAssignment): String {
    return defaultTireManifestBucket(item.adjustedExposure())
}
