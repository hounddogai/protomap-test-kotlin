package com.acme.transport.fleetops.inspection.review

import com.acme.transport.fleetops.inspection.slot.InspectionSlot
import com.acme.transport.fleetops.inspection.slot.inspectionSlotWeight

fun summarizeInspectionSlotsForPolicy(items: MutableList<InspectionSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackInspectionPoliciesFromSlot(items: MutableList<InspectionSlot>): MutableList<FallbackInspectionPolicy> {
    val out = mutableListOf<FallbackInspectionPolicy>()
    for (item in items) {
        val derived = newFallbackInspectionPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyFallbackInspectionPolicy(item: FallbackInspectionPolicy): Double = inspectionSlotWeight(9, true)
