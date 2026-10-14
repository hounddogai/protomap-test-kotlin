package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.inspection.estimate.InspectionLedger
import com.acme.transport.customs.inspection.estimate.classifyLenientInspectionEstimate

fun summarizeInspectionLedgersForEstimate(items: MutableList<InspectionLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveInspectionEstimatesFromLedger(items: MutableList<InspectionLedger>): MutableList<AdaptiveInspectionEstimate> {
    val out = mutableListOf<AdaptiveInspectionEstimate>()
    for (item in items) {
        val derived = newAdaptiveInspectionEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveInspectionEstimate(item: AdaptiveInspectionEstimate): String {
    return classifyLenientInspectionEstimate(item.effectiveValue())
}
