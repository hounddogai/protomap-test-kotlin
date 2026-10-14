package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.evcharge.chargeslot.EvChargeQuota
import com.acme.transport.emissions.evcharge.chargeslot.classifyEvChargeBatch

fun summarizeEvChargeQuotasForTarget(items: MutableList<EvChargeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRouteOffsetTargetsFromQuota(items: MutableList<EvChargeQuota>): MutableList<RouteOffsetTarget> {
    val out = mutableListOf<RouteOffsetTarget>()
    for (item in items) {
        val derived = newRouteOffsetTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRouteOffsetTarget(item: RouteOffsetTarget): String = classifyEvChargeBatch(item.netVolume())
