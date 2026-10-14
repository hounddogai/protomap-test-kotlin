package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.customs.clearance.cycle.ClearanceCycle
import com.acme.transport.customs.clearance.cycle.legacyClearanceCheckpointWeight

fun summarizeClearanceCyclesForQuota(items: MutableList<ClearanceCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveInspectionQuotasFromCycle(items: MutableList<ClearanceCycle>): MutableList<InspectionQuota> {
    val out = mutableListOf<InspectionQuota>()
    for (item in items) {
        val derived = newInspectionQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectInspectionQuota(item: InspectionQuota): Double = legacyClearanceCheckpointWeight(4, true)
