package com.acme.transport.network.transittime.proposal

import com.acme.transport.network.cutoff.adjustment.WeightedCutoffForecast
import com.acme.transport.network.cutoff.adjustment.legacyCutoffReservationBucket

fun summarizeWeightedCutoffForecastsForProfile(items: MutableList<WeightedCutoffForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTransitTimeProfilesFromForecast(items: MutableList<WeightedCutoffForecast>): MutableList<TransitTimeProfile> {
    val out = mutableListOf<TransitTimeProfile>()
    for (item in items) {
        val derived = newTransitTimeProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeTransitTimeProfile(item: TransitTimeProfile): String {
    return legacyCutoffReservationBucket(item.weightedVolume())
}
