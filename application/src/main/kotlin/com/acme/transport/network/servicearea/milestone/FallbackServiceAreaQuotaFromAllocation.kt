package com.acme.transport.network.servicearea.milestone

import com.acme.transport.network.zone.entry.AdaptiveZoneAllocation
import com.acme.transport.network.zone.entry.normalizeAdaptiveZoneAllocationCode

fun summarizeAdaptiveZoneAllocationsForQuota(items: MutableList<AdaptiveZoneAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveFallbackServiceAreaQuotasFromAllocation(items: MutableList<AdaptiveZoneAllocation>): MutableList<FallbackServiceAreaQuota> {
    val out = mutableListOf<FallbackServiceAreaQuota>()
    for (item in items) {
        val derived = newFallbackServiceAreaQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackServiceAreaQuota(item: FallbackServiceAreaQuota): String {
    return normalizeAdaptiveZoneAllocationCode(item.code)
}
