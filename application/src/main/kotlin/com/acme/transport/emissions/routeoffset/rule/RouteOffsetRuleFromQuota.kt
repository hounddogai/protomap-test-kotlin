package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.emissions.emissionfactor.batch.EmissionFactorQuota
import com.acme.transport.emissions.emissionfactor.batch.emissionFactorOverrideBucket

fun summarizeEmissionFactorQuotasForRule(items: MutableList<EmissionFactorQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRouteOffsetRulesFromQuota(items: MutableList<EmissionFactorQuota>): MutableList<RouteOffsetRule> {
    val out = mutableListOf<RouteOffsetRule>()
    for (item in items) {
        val derived = newRouteOffsetRule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRouteOffsetRule(item: RouteOffsetRule): String = emissionFactorOverrideBucket(item.adjustedTotal())
