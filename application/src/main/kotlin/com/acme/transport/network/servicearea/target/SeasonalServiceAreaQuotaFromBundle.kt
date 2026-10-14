package com.acme.transport.network.servicearea.target

import com.acme.transport.network.hub.hubentry.HubBundle

fun summarizeHubBundlesForQuota(items: MutableList<HubBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveSeasonalServiceAreaQuotasFromBundle(items: MutableList<HubBundle>): MutableList<SeasonalServiceAreaQuota> {
    val out = mutableListOf<SeasonalServiceAreaQuota>()
    for (item in items) {
        val derived = newSeasonalServiceAreaQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}
