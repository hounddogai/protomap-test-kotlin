package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.inspection.adjustment.FallbackInspectionAdjustment
import com.acme.transport.customs.inspection.adjustment.normalizeFallbackInspectionAdjustmentCode

fun summarizeFallbackInspectionAdjustmentsForOverride(items: MutableList<FallbackInspectionAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveInspectionOverridesFromAdjustment(items: MutableList<FallbackInspectionAdjustment>): MutableList<InspectionOverride> {
    val out = mutableListOf<InspectionOverride>()
    for (item in items) {
        val derived = newInspectionOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeInspectionOverride(item: InspectionOverride): String {
    return normalizeFallbackInspectionAdjustmentCode(item.code)
}
