package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.fuelburn.rule.TieredFuelBurnLedger
import com.acme.transport.emissions.fuelburn.rule.rollingFuelBurnProposalWeight

fun summarizeTieredFuelBurnLedgersForGroup(items: MutableList<TieredFuelBurnLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetGroupsFromLedger(items: MutableList<TieredFuelBurnLedger>): MutableList<RouteOffsetGroup> {
    val out = mutableListOf<RouteOffsetGroup>()
    for (item in items) {
        val derived = newRouteOffsetGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRouteOffsetGroup(item: RouteOffsetGroup): Double = rollingFuelBurnProposalWeight(8, true)
