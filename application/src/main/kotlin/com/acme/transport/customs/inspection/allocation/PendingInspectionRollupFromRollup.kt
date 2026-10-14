package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.inspection.draft.WeightedInspectionRollup
import com.acme.transport.customs.inspection.draft.classifyInspectionForecast

fun summarizeWeightedInspectionRollupsForRollup(items: MutableList<WeightedInspectionRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun derivePendingInspectionRollupsFromRollup(items: MutableList<WeightedInspectionRollup>): MutableList<PendingInspectionRollup> {
    val out = mutableListOf<PendingInspectionRollup>()
    for (item in items) {
        val derived = newPendingInspectionRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyPendingInspectionRollup(item: PendingInspectionRollup): String {
    return classifyInspectionForecast(item.effectiveTotal())
}
