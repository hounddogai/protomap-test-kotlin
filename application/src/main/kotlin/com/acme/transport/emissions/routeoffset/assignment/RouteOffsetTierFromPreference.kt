package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.emissions.evcharge.preference.EvChargePreference
import com.acme.transport.emissions.evcharge.preference.classifyLenientEvChargePreference

fun summarizeEvChargePreferencesForTier(items: MutableList<EvChargePreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRouteOffsetTiersFromPreference(items: MutableList<EvChargePreference>): MutableList<RouteOffsetTier> {
    val out = mutableListOf<RouteOffsetTier>()
    for (item in items) {
        val derived = newRouteOffsetTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignRouteOffsetTier(item: RouteOffsetTier): String = classifyLenientEvChargePreference(item.netLoad())
