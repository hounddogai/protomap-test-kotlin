package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.routeoffset.entry.RouteOffsetEntry

fun summarizeRouteOffsetEntriesForDigest(items: MutableList<RouteOffsetEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePendingRouteOffsetDigestsFromEntry(items: MutableList<RouteOffsetEntry>): MutableList<PendingRouteOffsetDigest> {
    val out = mutableListOf<PendingRouteOffsetDigest>()
    for (item in items) {
        val derived = newPendingRouteOffsetDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}
