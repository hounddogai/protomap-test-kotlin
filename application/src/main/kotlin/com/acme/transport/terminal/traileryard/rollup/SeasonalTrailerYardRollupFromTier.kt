package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.terminal.traileryard.score.WeightedTrailerYardTier
import com.acme.transport.terminal.traileryard.score.normalizeWeightedTrailerYardAllocationCode

fun summarizeWeightedTrailerYardTiersForRollup(items: MutableList<WeightedTrailerYardTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveSeasonalTrailerYardRollupsFromTier(items: MutableList<WeightedTrailerYardTier>): MutableList<SeasonalTrailerYardRollup> {
    val out = mutableListOf<SeasonalTrailerYardRollup>()
    for (item in items) {
        val derived = newSeasonalTrailerYardRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalTrailerYardRollupExtra(item: SeasonalTrailerYardRollup): String {
    return normalizeWeightedTrailerYardAllocationCode(item.code)
}
