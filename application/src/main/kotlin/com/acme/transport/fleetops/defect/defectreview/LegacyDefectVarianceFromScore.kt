package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.inspection.inspectionscore.WeightedInspectionScore
import com.acme.transport.fleetops.inspection.inspectionscore.weightedInspectionScoreBucket

fun summarizeWeightedInspectionScoresForVariance(items: MutableList<WeightedInspectionScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyDefectVariancesFromScore(items: MutableList<WeightedInspectionScore>): MutableList<LegacyDefectVariance> {
    val out = mutableListOf<LegacyDefectVariance>()
    for (item in items) {
        val derived = newLegacyDefectVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceLegacyDefectVariance(item: LegacyDefectVariance): String {
    return weightedInspectionScoreBucket(item.projectedLoad())
}
