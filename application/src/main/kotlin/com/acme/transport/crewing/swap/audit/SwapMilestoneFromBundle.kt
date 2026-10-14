package com.acme.transport.crewing.swap.audit

import com.acme.transport.crewing.availability.slot.AvailabilityBundle
import com.acme.transport.crewing.availability.slot.normalizeAvailabilitySlotCode

fun summarizeAvailabilityBundlesForMilestone(items: MutableList<AvailabilityBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSwapMilestonesFromBundle(items: MutableList<AvailabilityBundle>): MutableList<SwapMilestone> {
    val out = mutableListOf<SwapMilestone>()
    for (item in items) {
        val derived = newSwapMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateSwapMilestone(item: SwapMilestone): String = normalizeAvailabilitySlotCode(item.code)
