package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.terminal.crossdock.rule.PendingCrossDockVariance
import com.acme.transport.terminal.crossdock.rule.normalizeAdaptiveCrossDockRuleCode

fun summarizePendingCrossDockVariancesForPolicy(items: MutableList<PendingCrossDockVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveTrailerYardPoliciesFromVariance(items: MutableList<PendingCrossDockVariance>): MutableList<TrailerYardPolicy> {
    val out = mutableListOf<TrailerYardPolicy>()
    for (item in items) {
        val derived = newTrailerYardPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeTrailerYardPolicy(item: TrailerYardPolicy): String = normalizeAdaptiveCrossDockRuleCode(item.code)
