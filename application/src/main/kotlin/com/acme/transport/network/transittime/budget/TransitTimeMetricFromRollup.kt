package com.acme.transport.network.transittime.budget

import com.acme.transport.network.cutoff.cutoffledger.GlobalCutoffRollup
import com.acme.transport.network.cutoff.cutoffledger.globalCutoffRollupBucket

fun summarizeGlobalCutoffRollupsForMetric(items: MutableList<GlobalCutoffRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTransitTimeMetricsFromRollup(items: MutableList<GlobalCutoffRollup>): MutableList<TransitTimeMetric> {
    val out = mutableListOf<TransitTimeMetric>()
    for (item in items) {
        val derived = newTransitTimeMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateTransitTimeMetric(item: TransitTimeMetric): String = globalCutoffRollupBucket(item.netLoad())
