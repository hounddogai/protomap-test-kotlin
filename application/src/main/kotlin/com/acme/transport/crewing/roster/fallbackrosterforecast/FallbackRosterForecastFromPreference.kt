package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.availability.profile.PendingAvailabilityPreference
import com.acme.transport.crewing.availability.profile.pendingAvailabilityPreferenceWeight

fun summarizePendingAvailabilityPreferencesForForecast(items: MutableList<PendingAvailabilityPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveFallbackRosterForecastsFromPreference(items: MutableList<PendingAvailabilityPreference>): MutableList<FallbackRosterForecast> {
    val out = mutableListOf<FallbackRosterForecast>()
    for (item in items) {
        val derived = newFallbackRosterForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceFallbackRosterForecast(item: FallbackRosterForecast): Double {
    return pendingAvailabilityPreferenceWeight(8, true)
}
