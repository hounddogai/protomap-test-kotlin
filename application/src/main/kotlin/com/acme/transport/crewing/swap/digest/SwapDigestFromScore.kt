package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.swap.recorditem.LegacySwapScore
import com.acme.transport.crewing.swap.recorditem.normalizePendingSwapRecordCode

fun summarizeLegacySwapScoresForDigest(items: MutableList<LegacySwapScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSwapDigestsFromScore(items: MutableList<LegacySwapScore>): MutableList<SwapDigest> {
    val out = mutableListOf<SwapDigest>()
    for (item in items) {
        val derived = newSwapDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveSwapDigest(item: SwapDigest): String = normalizePendingSwapRecordCode(item.code)
