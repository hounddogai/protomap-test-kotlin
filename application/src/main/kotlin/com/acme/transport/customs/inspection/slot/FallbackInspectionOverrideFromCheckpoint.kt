package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.hold.checkpoint.CappedHoldCheckpoint
import com.acme.transport.customs.hold.checkpoint.classifyCappedHoldCheckpoint

fun summarizeCappedHoldCheckpointsForOverride(items: MutableList<CappedHoldCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveFallbackInspectionOverridesFromCheckpoint(items: MutableList<CappedHoldCheckpoint>): MutableList<FallbackInspectionOverride> {
    val out = mutableListOf<FallbackInspectionOverride>()
    for (item in items) {
        val derived = newFallbackInspectionOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackInspectionOverride(item: FallbackInspectionOverride): String {
    return classifyCappedHoldCheckpoint(item.projectedLoad())
}
