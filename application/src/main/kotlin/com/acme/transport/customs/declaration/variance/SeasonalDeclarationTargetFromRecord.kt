package com.acme.transport.customs.declaration.variance

import com.acme.transport.customs.inspection.target.ManualInspectionRecord
import com.acme.transport.customs.inspection.target.normalizeInspectionReviewCode

fun summarizeManualInspectionRecordsForTarget(items: MutableList<ManualInspectionRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalDeclarationTargetsFromRecord(items: MutableList<ManualInspectionRecord>): MutableList<SeasonalDeclarationTarget> {
    val out = mutableListOf<SeasonalDeclarationTarget>()
    for (item in items) {
        val derived = newSeasonalDeclarationTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalDeclarationTarget(item: SeasonalDeclarationTarget): String {
    return normalizeInspectionReviewCode(item.code)
}
