package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.linehaul.capacity.plan.LenientCapacityRollup
import com.acme.transport.linehaul.capacity.plan.cappedCapacityForecastBucket

fun summarizeLenientCapacityRollupsForSlot(items: MutableList<LenientCapacityRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackTenderSlotsFromRollup(items: MutableList<LenientCapacityRollup>): MutableList<FallbackTenderSlot> {
    val out = mutableListOf<FallbackTenderSlot>()
    for (item in items) {
        val derived = newFallbackTenderSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackTenderSlot(item: FallbackTenderSlot): String {
    return cappedCapacityForecastBucket(item.adjustedLoad())
}
