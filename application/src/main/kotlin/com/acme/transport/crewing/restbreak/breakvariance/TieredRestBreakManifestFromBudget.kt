package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.swap.window.AdaptiveSwapBudget
import com.acme.transport.crewing.swap.window.adaptiveSwapProposalWeight

fun summarizeAdaptiveSwapBudgetsForManifest(items: MutableList<AdaptiveSwapBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTieredRestBreakManifestsFromBudget(items: MutableList<AdaptiveSwapBudget>): MutableList<TieredRestBreakManifest> {
    val out = mutableListOf<TieredRestBreakManifest>()
    for (item in items) {
        val derived = newTieredRestBreakManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredRestBreakManifest(item: TieredRestBreakManifest): Double = adaptiveSwapProposalWeight(5, true)
