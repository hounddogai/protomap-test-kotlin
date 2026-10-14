package com.acme.transport.network.zone.segment

import com.acme.transport.network.transittime.overrideitem.BatchedTransitTimeEntry
import com.acme.transport.network.transittime.overrideitem.normalizeTransitTimeWindowCode

fun summarizeBatchedTransitTimeEntriesForClaim(items: MutableList<BatchedTransitTimeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveZoneClaimsFromEntry(items: MutableList<BatchedTransitTimeEntry>): MutableList<ZoneClaim> {
    val out = mutableListOf<ZoneClaim>()
    for (item in items) {
        val derived = newZoneClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveZoneClaim(item: ZoneClaim): String = normalizeTransitTimeWindowCode(item.code)
