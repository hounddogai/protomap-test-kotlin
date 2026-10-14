package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.linehaul.relay.schedule.RelayBaseline
import com.acme.transport.linehaul.relay.schedule.tieredRelayOverrideBucket

fun summarizeRelayBaselinesForRequest(items: MutableList<RelayBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientCapacityRequestsFromBaseline(items: MutableList<RelayBaseline>): MutableList<LenientCapacityRequest> {
    val out = mutableListOf<LenientCapacityRequest>()
    for (item in items) {
        val derived = newLenientCapacityRequest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientCapacityRequest(item: LenientCapacityRequest): String {
    return tieredRelayOverrideBucket(item.effectiveLoad())
}
