package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.routeoffset.slot.CappedRouteOffsetRequest
import com.acme.transport.emissions.routeoffset.slot.routeOffsetSlotWeight

fun summarizeCappedRouteOffsetRequestsForRequest(items: MutableList<CappedRouteOffsetRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRollingIdlingRequestsFromRequest(items: MutableList<CappedRouteOffsetRequest>): MutableList<RollingIdlingRequest> {
    val out = mutableListOf<RollingIdlingRequest>()
    for (item in items) {
        val derived = newRollingIdlingRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRollingIdlingRequest(item: RollingIdlingRequest): Double = routeOffsetSlotWeight(6, true)
