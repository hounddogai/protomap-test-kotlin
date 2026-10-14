package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.idling.budget.PendingIdlingForecast
import com.acme.transport.emissions.idling.budget.pendingIdlingForecastBucket

fun summarizePendingIdlingForecastsForNotice(items: MutableList<PendingIdlingForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackRouteOffsetNoticesFromForecast(items: MutableList<PendingIdlingForecast>): MutableList<FallbackRouteOffsetNotice> {
    val out = mutableListOf<FallbackRouteOffsetNotice>()
    for (item in items) {
        val derived = newFallbackRouteOffsetNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planFallbackRouteOffsetNotice(item: FallbackRouteOffsetNotice): String {
    return pendingIdlingForecastBucket(item.effectiveTotal())
}
