package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.terminal.dwell.ledger.FallbackDwellBundle
import com.acme.transport.terminal.dwell.ledger.normalizeCappedDwellBatchCode

fun summarizeFallbackDwellBundlesForBaseline(items: MutableList<FallbackDwellBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSortationBaselinesFromBundle(items: MutableList<FallbackDwellBundle>): MutableList<SortationBaseline> {
    val out = mutableListOf<SortationBaseline>()
    for (item in items) {
        val derived = newSortationBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankSortationBaseline(item: SortationBaseline): String = normalizeCappedDwellBatchCode(item.code)
