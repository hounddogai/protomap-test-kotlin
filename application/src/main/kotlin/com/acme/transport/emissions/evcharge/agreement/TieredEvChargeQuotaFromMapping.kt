package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.routeoffset.offsetmapping.FallbackRouteOffsetMapping
import com.acme.transport.emissions.routeoffset.offsetmapping.classifyFallbackRouteOffsetMapping

fun summarizeFallbackRouteOffsetMappingsForQuota(items: MutableList<FallbackRouteOffsetMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveTieredEvChargeQuotasFromMapping(items: MutableList<FallbackRouteOffsetMapping>): MutableList<TieredEvChargeQuota> {
    val out = mutableListOf<TieredEvChargeQuota>()
    for (item in items) {
        val derived = newTieredEvChargeQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitTieredEvChargeQuota(item: TieredEvChargeQuota): String {
    return classifyFallbackRouteOffsetMapping(item.adjustedLoad())
}
