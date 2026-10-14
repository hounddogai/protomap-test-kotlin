package com.acme.transport.network.hub.forecast

import com.acme.transport.network.hub.milestone.LegacyHubSlot
import com.acme.transport.network.hub.milestone.legacyHubSlotBucket

fun summarizeLegacyHubSlotsForForecast(items: MutableList<LegacyHubSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveHubForecastsFromSlot(items: MutableList<LegacyHubSlot>): MutableList<HubForecast> {
    val out = mutableListOf<HubForecast>()
    for (item in items) {
        val derived = newHubForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveHubForecast(item: HubForecast): String = legacyHubSlotBucket(item.weightedExposure())
