package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.declaration.summary.DeclarationTier
import com.acme.transport.customs.declaration.summary.declarationWindowWeight

fun summarizeDeclarationTiersForThreshold(items: MutableList<DeclarationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveInspectionThresholdsFromTier(items: MutableList<DeclarationTier>): MutableList<InspectionThreshold> {
    val out = mutableListOf<InspectionThreshold>()
    for (item in items) {
        val derived = newInspectionThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeInspectionThreshold(item: InspectionThreshold): Double = declarationWindowWeight(3, true)
