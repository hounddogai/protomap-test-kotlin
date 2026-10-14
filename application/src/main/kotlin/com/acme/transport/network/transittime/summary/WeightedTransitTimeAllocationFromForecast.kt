package com.acme.transport.network.transittime.summary

import com.acme.transport.network.servicearea.areabatch.FallbackServiceAreaForecast
import com.acme.transport.network.servicearea.areabatch.normalizeWeightedServiceAreaAgreementCode

fun summarizeFallbackServiceAreaForecastsForAllocation(items: MutableList<FallbackServiceAreaForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveWeightedTransitTimeAllocationsFromForecast(items: MutableList<FallbackServiceAreaForecast>): MutableList<WeightedTransitTimeAllocation> {
    val out = mutableListOf<WeightedTransitTimeAllocation>()
    for (item in items) {
        val derived = newWeightedTransitTimeAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveWeightedTransitTimeAllocation(item: WeightedTransitTimeAllocation): String {
    return normalizeWeightedServiceAreaAgreementCode(item.code)
}
