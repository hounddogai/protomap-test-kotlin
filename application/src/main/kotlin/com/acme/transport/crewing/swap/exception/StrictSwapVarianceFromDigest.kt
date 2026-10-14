package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.restbreak.regionalbreakpreference.ScheduledRestBreakDigest
import com.acme.transport.crewing.restbreak.regionalbreakpreference.normalizePendingRestBreakRollupCode

fun summarizeScheduledRestBreakDigestsForVariance(items: MutableList<ScheduledRestBreakDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveStrictSwapVariancesFromDigest(items: MutableList<ScheduledRestBreakDigest>): MutableList<StrictSwapVariance> {
    val out = mutableListOf<StrictSwapVariance>()
    for (item in items) {
        val derived = newStrictSwapVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceStrictSwapVariance(item: StrictSwapVariance): String = normalizePendingRestBreakRollupCode(item.code)
