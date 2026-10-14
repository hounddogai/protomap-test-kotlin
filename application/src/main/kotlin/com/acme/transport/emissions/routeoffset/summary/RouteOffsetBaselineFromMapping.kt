package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.fuelburn.notice.FuelBurnMapping
import com.acme.transport.emissions.fuelburn.notice.normalizeFuelBurnMappingCode

fun summarizeFuelBurnMappingsForBaseline(items: MutableList<FuelBurnMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetBaselinesFromMapping(items: MutableList<FuelBurnMapping>): MutableList<RouteOffsetBaseline> {
    val out = mutableListOf<RouteOffsetBaseline>()
    for (item in items) {
        val derived = newRouteOffsetBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRouteOffsetBaseline(item: RouteOffsetBaseline): String = normalizeFuelBurnMappingCode(item.code)
