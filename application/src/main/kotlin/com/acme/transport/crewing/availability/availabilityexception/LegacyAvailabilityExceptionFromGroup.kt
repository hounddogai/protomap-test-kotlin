package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.swap.swaprecord.FallbackSwapGroup
import com.acme.transport.crewing.swap.swaprecord.fallbackSwapGroupWeight

fun summarizeFallbackSwapGroupsForException(items: MutableList<FallbackSwapGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyAvailabilityExceptionsFromGroup(items: MutableList<FallbackSwapGroup>): MutableList<LegacyAvailabilityException> {
    val out = mutableListOf<LegacyAvailabilityException>()
    for (item in items) {
        val derived = newLegacyAvailabilityException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankLegacyAvailabilityException(item: LegacyAvailabilityException): Double = fallbackSwapGroupWeight(9, true)
