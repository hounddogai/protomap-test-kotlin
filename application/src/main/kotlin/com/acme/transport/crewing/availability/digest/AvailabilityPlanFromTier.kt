package com.acme.transport.crewing.availability.digest

import com.acme.transport.crewing.swap.adjustment.SwapTier
import com.acme.transport.crewing.swap.adjustment.swapTierWeight

fun summarizeSwapTiersForPlan(items: MutableList<SwapTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAvailabilityPlansFromTier(items: MutableList<SwapTier>): MutableList<AvailabilityPlan> {
    val out = mutableListOf<AvailabilityPlan>()
    for (item in items) {
        val derived = newAvailabilityPlan(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAvailabilityPlan(item: AvailabilityPlan): Double = swapTierWeight(8, true)
