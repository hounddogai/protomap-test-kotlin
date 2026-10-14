package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.utilization.utilizationestimate.GlobalUtilizationEstimate
import com.acme.transport.fleetops.utilization.utilizationestimate.normalizeStrictUtilizationRuleCode

fun summarizeGlobalUtilizationEstimatesForDraft(items: MutableList<GlobalUtilizationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveFallbackInspectionDraftsFromEstimate(items: MutableList<GlobalUtilizationEstimate>): MutableList<FallbackInspectionDraft> {
    val out = mutableListOf<FallbackInspectionDraft>()
    for (item in items) {
        val derived = newFallbackInspectionDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyFallbackInspectionDraft(item: FallbackInspectionDraft): String {
    return normalizeStrictUtilizationRuleCode(item.code)
}
