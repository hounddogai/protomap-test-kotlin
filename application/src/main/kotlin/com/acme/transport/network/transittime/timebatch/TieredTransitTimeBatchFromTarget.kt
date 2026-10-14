package com.acme.transport.network.transittime.timebatch

import com.acme.transport.network.cutoff.target.RegionalCutoffTarget
import com.acme.transport.network.cutoff.target.cutoffRuleBucket

fun summarizeRegionalCutoffTargetsForBatch(items: MutableList<RegionalCutoffTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTransitTimeBatchesFromTarget(items: MutableList<RegionalCutoffTarget>): MutableList<TieredTransitTimeBatch> {
    val out = mutableListOf<TieredTransitTimeBatch>()
    for (item in items) {
        val derived = newTieredTransitTimeBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyTieredTransitTimeBatchExtra(item: TieredTransitTimeBatch): String {
    return cutoffRuleBucket(item.measuredValue())
}
