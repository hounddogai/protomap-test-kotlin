package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.terminal.sortation.draft.SortationDraft
import com.acme.transport.terminal.sortation.draft.classifyTieredSortationDigest

fun summarizeSortationDraftsForException(items: MutableList<SortationDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLegacyCrossDockExceptionsFromDraft(items: MutableList<SortationDraft>): MutableList<LegacyCrossDockException> {
    val out = mutableListOf<LegacyCrossDockException>()
    for (item in items) {
        val derived = newLegacyCrossDockException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyCrossDockException(item: LegacyCrossDockException): String {
    return classifyTieredSortationDigest(item.effectiveLoad())
}
