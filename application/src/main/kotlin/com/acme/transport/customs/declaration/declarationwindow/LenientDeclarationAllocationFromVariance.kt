package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.inspection.schedule.InspectionVariance
import com.acme.transport.customs.inspection.schedule.classifyBatchedInspectionTemplate

fun summarizeInspectionVariancesForAllocation(items: MutableList<InspectionVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLenientDeclarationAllocationsFromVariance(items: MutableList<InspectionVariance>): MutableList<LenientDeclarationAllocation> {
    val out = mutableListOf<LenientDeclarationAllocation>()
    for (item in items) {
        val derived = newLenientDeclarationAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientDeclarationAllocation(item: LenientDeclarationAllocation): String {
    return classifyBatchedInspectionTemplate(item.netVolume())
}
