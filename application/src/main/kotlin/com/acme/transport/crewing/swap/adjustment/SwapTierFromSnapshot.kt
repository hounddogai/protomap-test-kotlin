package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.restbreak.breakpreference.LegacyRestBreakSnapshot
import com.acme.transport.crewing.restbreak.breakpreference.normalizeRegionalRestBreakRuleCode

fun summarizeLegacyRestBreakSnapshotsForTier(items: MutableList<LegacyRestBreakSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSwapTiersFromSnapshot(items: MutableList<LegacyRestBreakSnapshot>): MutableList<SwapTier> {
    val out = mutableListOf<SwapTier>()
    for (item in items) {
        val derived = newSwapTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleSwapTier(item: SwapTier): String = normalizeRegionalRestBreakRuleCode(item.code)
