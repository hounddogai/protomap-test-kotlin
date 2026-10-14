package com.acme.transport.terminal.sortation.draft

import com.acme.transport.terminal.sortation.sortationtier.ScheduledSortationTier
import com.acme.transport.terminal.sortation.sortationtier.normalizeSortationProposalCode

fun summarizeScheduledSortationTiersForDigest(items: MutableList<ScheduledSortationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredSortationDigestsFromTier(items: MutableList<ScheduledSortationTier>): MutableList<TieredSortationDigest> {
    val out = mutableListOf<TieredSortationDigest>()
    for (item in items) {
        val derived = newTieredSortationDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankTieredSortationDigest(item: TieredSortationDigest): String = normalizeSortationProposalCode(item.code)
