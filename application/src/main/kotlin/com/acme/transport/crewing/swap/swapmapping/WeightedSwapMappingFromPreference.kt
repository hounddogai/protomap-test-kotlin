package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.availability.availabilityledger.CappedAvailabilityPreference
import com.acme.transport.crewing.availability.availabilityledger.globalAvailabilityForecastBucket

fun summarizeCappedAvailabilityPreferencesForMapping(items: MutableList<CappedAvailabilityPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedSwapMappingsFromPreference(items: MutableList<CappedAvailabilityPreference>): MutableList<WeightedSwapMapping> {
    val out = mutableListOf<WeightedSwapMapping>()
    for (item in items) {
        val derived = newWeightedSwapMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeWeightedSwapMapping(item: WeightedSwapMapping): String {
    return globalAvailabilityForecastBucket(item.effectiveVolume())
}
