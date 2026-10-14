package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.network.cutoff.slot.CutoffBundle
import com.acme.transport.network.cutoff.slot.normalizeCutoffSlotCode

fun summarizeCutoffBundlesForEntry(items: MutableList<CutoffBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedServiceAreaEntriesFromBundle(items: MutableList<CutoffBundle>): MutableList<BatchedServiceAreaEntry> {
    val out = mutableListOf<BatchedServiceAreaEntry>()
    for (item in items) {
        val derived = newBatchedServiceAreaEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceBatchedServiceAreaEntry(item: BatchedServiceAreaEntry): String = normalizeCutoffSlotCode(item.code)
