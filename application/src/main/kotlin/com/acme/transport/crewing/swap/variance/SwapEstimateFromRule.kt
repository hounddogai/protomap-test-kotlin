package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.swap.rule.TieredSwapRule
import com.acme.transport.crewing.swap.rule.classifyTieredSwapRule

fun summarizeTieredSwapRulesForEstimate(items: MutableList<TieredSwapRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveSwapEstimatesFromRule(items: MutableList<TieredSwapRule>): MutableList<SwapEstimate> {
    val out = mutableListOf<SwapEstimate>()
    for (item in items) {
        val derived = newSwapEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchSwapEstimate(item: SwapEstimate): String = classifyTieredSwapRule(item.projectedVolume())
