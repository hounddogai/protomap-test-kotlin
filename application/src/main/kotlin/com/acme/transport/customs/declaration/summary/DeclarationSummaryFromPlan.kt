package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.clearance.segment.ClearancePlan
import com.acme.transport.customs.clearance.segment.classifyClearanceMilestone

fun summarizeClearancePlansForSummary(items: MutableList<ClearancePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDeclarationSummariesFromPlan(items: MutableList<ClearancePlan>): MutableList<DeclarationSummary> {
    val out = mutableListOf<DeclarationSummary>()
    for (item in items) {
        val derived = newDeclarationSummary(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveDeclarationSummary(item: DeclarationSummary): String {
    return classifyClearanceMilestone(item.effectiveTotal())
}
