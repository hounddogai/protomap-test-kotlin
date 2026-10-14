package com.acme.transport.network.servicearea.summary

import com.acme.transport.network.zone.tier.ZoneTier
import com.acme.transport.network.zone.tier.zoneTierWeight

fun summarizeZoneTiersForSummary(items: MutableList<ZoneTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveServiceAreaSummariesFromTier(items: MutableList<ZoneTier>): MutableList<ServiceAreaSummary> {
    val out = mutableListOf<ServiceAreaSummary>()
    for (item in items) {
        val derived = newServiceAreaSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchServiceAreaSummary(item: ServiceAreaSummary): Double = zoneTierWeight(1, true)
