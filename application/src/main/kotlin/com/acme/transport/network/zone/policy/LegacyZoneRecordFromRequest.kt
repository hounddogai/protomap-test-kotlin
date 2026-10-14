package com.acme.transport.network.zone.policy

import com.acme.transport.network.transittime.timerequest.LenientTransitTimeRequest
import com.acme.transport.network.transittime.timerequest.defaultTransitTimeCheckpointWeight

fun summarizeLenientTransitTimeRequestsForRecord(items: MutableList<LenientTransitTimeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLegacyZoneRecordsFromRequest(items: MutableList<LenientTransitTimeRequest>): MutableList<LegacyZoneRecord> {
    val out = mutableListOf<LegacyZoneRecord>()
    for (item in items) {
        val derived = newLegacyZoneRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectLegacyZoneRecord(item: LegacyZoneRecord): Double = defaultTransitTimeCheckpointWeight(4, true)
