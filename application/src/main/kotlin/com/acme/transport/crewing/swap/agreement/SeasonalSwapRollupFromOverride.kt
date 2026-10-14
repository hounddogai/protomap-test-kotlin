package com.acme.transport.crewing.swap.agreement

import com.acme.transport.crewing.restbreak.milestone.FallbackRestBreakOverride
import com.acme.transport.crewing.restbreak.milestone.classifyFallbackRestBreakOverride

fun summarizeFallbackRestBreakOverridesForRollup(items: MutableList<FallbackRestBreakOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalSwapRollupsFromOverride(items: MutableList<FallbackRestBreakOverride>): MutableList<SeasonalSwapRollup> {
    val out = mutableListOf<SeasonalSwapRollup>()
    for (item in items) {
        val derived = newSeasonalSwapRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalSwapRollup(item: SeasonalSwapRollup): String {
    return classifyFallbackRestBreakOverride(item.adjustedExposure())
}
