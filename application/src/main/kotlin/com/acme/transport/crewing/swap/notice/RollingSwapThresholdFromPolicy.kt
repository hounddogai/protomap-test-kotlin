package com.acme.transport.crewing.swap.notice

import com.acme.transport.crewing.swap.policy.SwapPolicy
import com.acme.transport.crewing.swap.policy.defaultSwapOverrideWeight

fun summarizeSwapPoliciesForThreshold(items: MutableList<SwapPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRollingSwapThresholdsFromPolicy(items: MutableList<SwapPolicy>): MutableList<RollingSwapThreshold> {
    val out = mutableListOf<RollingSwapThreshold>()
    for (item in items) {
        val derived = newRollingSwapThreshold(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRollingSwapThreshold(item: RollingSwapThreshold): Double = defaultSwapOverrideWeight(5, true)
