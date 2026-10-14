package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.linehaul.relay.relaythreshold.RelayForecast
import com.acme.transport.linehaul.relay.relaythreshold.normalizeRegionalRelayThresholdCode

fun summarizeRelayForecastsForPreference(items: MutableList<RelayForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveWeightedCapacityPreferencesFromForecast(items: MutableList<RelayForecast>): MutableList<WeightedCapacityPreference> {
    val out = mutableListOf<WeightedCapacityPreference>()
    for (item in items) {
        val derived = newWeightedCapacityPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleWeightedCapacityPreference(item: WeightedCapacityPreference): String {
    return normalizeRegionalRelayThresholdCode(item.code)
}
