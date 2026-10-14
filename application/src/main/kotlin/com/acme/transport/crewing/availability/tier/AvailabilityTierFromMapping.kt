package com.acme.transport.crewing.availability.tier

import com.acme.transport.crewing.swap.mapping.SwapMapping
import com.acme.transport.crewing.swap.mapping.normalizeSeasonalSwapSegmentCode

fun summarizeSwapMappingsForTier(items: MutableList<SwapMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveAvailabilityTiersFromMapping(items: MutableList<SwapMapping>): MutableList<AvailabilityTier> {
    val out = mutableListOf<AvailabilityTier>()
    for (item in items) {
        val derived = newAvailabilityTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankAvailabilityTier(item: AvailabilityTier): String = normalizeSeasonalSwapSegmentCode(item.code)
