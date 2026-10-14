package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.fleetops.utilization.utilizationsummary.FallbackUtilizationTarget
import com.acme.transport.fleetops.utilization.utilizationsummary.classifySeasonalUtilizationTarget

fun summarizeFallbackUtilizationTargetsForMapping(items: MutableList<FallbackUtilizationTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryInspectionMappingsFromTarget(items: MutableList<FallbackUtilizationTarget>): MutableList<PrimaryInspectionMapping> {
    val out = mutableListOf<PrimaryInspectionMapping>()
    for (item in items) {
        val derived = newPrimaryInspectionMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryInspectionMapping(item: PrimaryInspectionMapping): String {
    return classifySeasonalUtilizationTarget(item.adjustedVolume())
}
