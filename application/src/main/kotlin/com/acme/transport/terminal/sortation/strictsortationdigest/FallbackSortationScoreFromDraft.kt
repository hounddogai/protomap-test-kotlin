package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.terminal.dwell.review.DwellDraft
import com.acme.transport.terminal.dwell.review.normalizeGlobalDwellProposalCode

fun summarizeDwellDraftsForScore(items: MutableList<DwellDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveFallbackSortationScoresFromDraft(items: MutableList<DwellDraft>): MutableList<FallbackSortationScore> {
    val out = mutableListOf<FallbackSortationScore>()
    for (item in items) {
        val derived = newFallbackSortationScore(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectFallbackSortationScore(item: FallbackSortationScore): String {
    return normalizeGlobalDwellProposalCode(item.code)
}
