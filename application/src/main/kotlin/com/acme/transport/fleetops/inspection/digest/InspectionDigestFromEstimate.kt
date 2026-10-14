package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.fleetops.utilization.target.UtilizationEstimate
import com.acme.transport.fleetops.utilization.target.utilizationMappingWeight

fun summarizeUtilizationEstimatesForDigest(items: MutableList<UtilizationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveInspectionDigestsFromEstimate(items: MutableList<UtilizationEstimate>): MutableList<InspectionDigest> {
    val out = mutableListOf<InspectionDigest>()
    for (item in items) {
        val derived = newInspectionDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeInspectionDigest(item: InspectionDigest): Double = utilizationMappingWeight(8, true)
