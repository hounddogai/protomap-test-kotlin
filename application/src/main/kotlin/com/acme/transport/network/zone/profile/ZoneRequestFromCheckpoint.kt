package com.acme.transport.network.zone.profile

import com.acme.transport.network.transittime.bundle.SeasonalTransitTimeCheckpoint
import com.acme.transport.network.transittime.bundle.tieredTransitTimeBudgetBucket

fun summarizeSeasonalTransitTimeCheckpointsForRequest(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveZoneRequestsFromCheckpoint(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableList<ZoneRequest> {
    val out = mutableListOf<ZoneRequest>()
    for (item in items) {
        val derived = newZoneRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchZoneRequest(item: ZoneRequest): String = tieredTransitTimeBudgetBucket(item.measuredValue())
