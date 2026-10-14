package com.acme.transport.network.transittime.slot

import com.acme.transport.network.hub.mapping.HubLedger
import com.acme.transport.network.hub.mapping.hubMappingBucket

fun summarizeHubLedgersForSegment(items: MutableList<HubLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePrimaryTransitTimeSegmentsFromLedger(items: MutableList<HubLedger>): MutableList<PrimaryTransitTimeSegment> {
    val out = mutableListOf<PrimaryTransitTimeSegment>()
    for (item in items) {
        val derived = newPrimaryTransitTimeSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchPrimaryTransitTimeSegment(item: PrimaryTransitTimeSegment): String {
    return hubMappingBucket(item.effectiveLoad())
}
