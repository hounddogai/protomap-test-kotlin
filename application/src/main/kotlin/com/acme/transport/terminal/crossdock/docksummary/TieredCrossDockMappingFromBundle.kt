package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.terminal.sortation.metric.WeightedSortationBundle
import com.acme.transport.terminal.sortation.metric.weightedSortationBundleBucket

fun summarizeWeightedSortationBundlesForMapping(items: MutableList<WeightedSortationBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTieredCrossDockMappingsFromBundle(items: MutableList<WeightedSortationBundle>): MutableList<TieredCrossDockMapping> {
    val out = mutableListOf<TieredCrossDockMapping>()
    for (item in items) {
        val derived = newTieredCrossDockMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeTieredCrossDockMapping(item: TieredCrossDockMapping): String {
    return weightedSortationBundleBucket(item.effectiveTotal())
}
