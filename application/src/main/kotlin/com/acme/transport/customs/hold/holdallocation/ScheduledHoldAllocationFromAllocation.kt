package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.inspection.inspectiondigest.AdaptiveInspectionAllocation
import com.acme.transport.customs.inspection.inspectiondigest.seasonalInspectionDigestWeight

fun summarizeAdaptiveInspectionAllocationsForAllocation(items: MutableList<AdaptiveInspectionAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveScheduledHoldAllocationsFromAllocation(items: MutableList<AdaptiveInspectionAllocation>): MutableList<ScheduledHoldAllocation> {
    val out = mutableListOf<ScheduledHoldAllocation>()
    for (item in items) {
        val derived = newScheduledHoldAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledHoldAllocation(item: ScheduledHoldAllocation): Double = seasonalInspectionDigestWeight(4, true)
