package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.crewing.swap.policy.AdaptiveSwapWindow
import com.acme.transport.crewing.swap.policy.adaptiveSwapWindowBucket

fun summarizeAdaptiveSwapWindowsForThreshold(items: MutableList<AdaptiveSwapWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTieredRestBreakThresholdsFromWindow(items: MutableList<AdaptiveSwapWindow>): MutableList<TieredRestBreakThreshold> {
    val out = mutableListOf<TieredRestBreakThreshold>()
    for (item in items) {
        val derived = newTieredRestBreakThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreTieredRestBreakThreshold(item: TieredRestBreakThreshold): String {
    return adaptiveSwapWindowBucket(item.weightedTotal())
}
