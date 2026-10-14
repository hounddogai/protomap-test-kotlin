package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.terminal.sortation.bundle.PrimarySortationBatch
import com.acme.transport.terminal.sortation.bundle.sortationSegmentBucket

fun summarizePrimarySortationBatchesForOverride(items: MutableList<PrimarySortationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSortationOverridesFromBatch(items: MutableList<PrimarySortationBatch>): MutableList<SortationOverride> {
    val out = mutableListOf<SortationOverride>()
    for (item in items) {
        val derived = newSortationOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateSortationOverride(item: SortationOverride): String = sortationSegmentBucket(item.projectedLoad())
