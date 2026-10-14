package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.fleetops.tire.claim.CappedTireTemplate
import com.acme.transport.fleetops.tire.claim.classifyCappedTireTemplate

fun summarizeCappedTireTemplatesForScore(items: MutableList<CappedTireTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveWeightedInspectionScoresFromTemplate(items: MutableList<CappedTireTemplate>): MutableList<WeightedInspectionScore> {
    val out = mutableListOf<WeightedInspectionScore>()
    for (item in items) {
        val derived = newWeightedInspectionScore(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedInspectionScore(item: WeightedInspectionScore): String {
    return classifyCappedTireTemplate(item.weightedExposure())
}
