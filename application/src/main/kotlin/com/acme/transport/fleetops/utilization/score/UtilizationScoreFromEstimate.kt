package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.inspection.estimate.InspectionEstimate
import com.acme.transport.fleetops.inspection.estimate.legacyInspectionClaimBucket

fun summarizeInspectionEstimatesForScore(items: MutableList<InspectionEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveUtilizationScoresFromEstimate(items: MutableList<InspectionEstimate>): MutableList<UtilizationScore> {
    val out = mutableListOf<UtilizationScore>()
    for (item in items) {
        val derived = newUtilizationScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planUtilizationScore(item: UtilizationScore): String = legacyInspectionClaimBucket(item.projectedValue())
