package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.restbreak.entry.RestBreakEntry
import com.acme.transport.crewing.restbreak.entry.restBreakCycleWeight

fun summarizeRestBreakEntriesForVariance(items: MutableList<RestBreakEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBatchedSwapVariancesFromEntry(items: MutableList<RestBreakEntry>): MutableList<BatchedSwapVariance> {
    val out = mutableListOf<BatchedSwapVariance>()
    for (item in items) {
        val derived = newBatchedSwapVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBatchedSwapVariance(item: BatchedSwapVariance): Double = restBreakCycleWeight(9, true)
