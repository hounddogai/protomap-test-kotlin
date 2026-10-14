package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.utilization.utilizationsummary.SeasonalUtilizationTarget
import com.acme.transport.fleetops.utilization.utilizationsummary.fallbackUtilizationTargetWeight

fun summarizeSeasonalUtilizationTargetsForRollup(items: MutableList<SeasonalUtilizationTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualDefectRollupsFromTarget(items: MutableList<SeasonalUtilizationTarget>): MutableList<ManualDefectRollup> {
    val out = mutableListOf<ManualDefectRollup>()
    for (item in items) {
        val derived = newManualDefectRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualDefectRollup(item: ManualDefectRollup): Double = fallbackUtilizationTargetWeight(6, true)
