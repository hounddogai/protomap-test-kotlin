package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.lastmile.drop.rollup.DropThreshold
import com.acme.transport.lastmile.drop.rollup.tieredDropNoticeWeight

fun summarizeDropThresholdsForPolicy(items: MutableList<DropThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFallbackAttemptPoliciesFromThreshold(items: MutableList<DropThreshold>): MutableList<FallbackAttemptPolicy> {
    val out = mutableListOf<FallbackAttemptPolicy>()
    for (item in items) {
        val derived = newFallbackAttemptPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackAttemptPolicy(item: FallbackAttemptPolicy): Double = tieredDropNoticeWeight(7, true)
