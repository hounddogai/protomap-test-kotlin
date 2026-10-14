package com.acme.transport.network.zone.digest

import com.acme.transport.network.servicearea.request.AdaptiveServiceAreaCheckpoint
import com.acme.transport.network.servicearea.request.classifyFallbackServiceAreaAudit

fun summarizeAdaptiveServiceAreaCheckpointsForAssignment(items: MutableList<AdaptiveServiceAreaCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLenientZoneAssignmentsFromCheckpoint(items: MutableList<AdaptiveServiceAreaCheckpoint>): MutableList<LenientZoneAssignment> {
    val out = mutableListOf<LenientZoneAssignment>()
    for (item in items) {
        val derived = newLenientZoneAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankLenientZoneAssignment(item: LenientZoneAssignment): String {
    return classifyFallbackServiceAreaAudit(item.projectedVolume())
}
