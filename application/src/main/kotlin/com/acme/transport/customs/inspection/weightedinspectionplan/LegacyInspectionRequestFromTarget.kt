package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.clearance.slot.DefaultClearanceTarget
import com.acme.transport.customs.clearance.slot.classifyClearanceCheckpoint

fun summarizeDefaultClearanceTargetsForRequest(items: MutableList<DefaultClearanceTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLegacyInspectionRequestsFromTarget(items: MutableList<DefaultClearanceTarget>): MutableList<LegacyInspectionRequest> {
    val out = mutableListOf<LegacyInspectionRequest>()
    for (item in items) {
        val derived = newLegacyInspectionRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLegacyInspectionRequest(item: LegacyInspectionRequest): String {
    return classifyClearanceCheckpoint(item.netExposure())
}
