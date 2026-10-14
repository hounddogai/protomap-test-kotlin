package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.emissions.fuelburn.estimate.FuelBurnDraft
import com.acme.transport.emissions.fuelburn.estimate.fuelBurnDraftWeight

fun summarizeFuelBurnDraftsForMapping(items: MutableList<FuelBurnDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveFallbackRouteOffsetMappingsFromDraft(items: MutableList<FuelBurnDraft>): MutableList<FallbackRouteOffsetMapping> {
    val out = mutableListOf<FallbackRouteOffsetMapping>()
    for (item in items) {
        val derived = newFallbackRouteOffsetMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchFallbackRouteOffsetMapping(item: FallbackRouteOffsetMapping): Double = fuelBurnDraftWeight(1, true)
