package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.crewing.swap.adjustment.SwapAdjustment
import com.acme.transport.crewing.swap.adjustment.classifySwapTier

fun summarizeSwapAdjustmentsForProfile(items: MutableList<SwapAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalDutyPeriodProfilesFromAdjustment(items: MutableList<SwapAdjustment>): MutableList<RegionalDutyPeriodProfile> {
    val out = mutableListOf<RegionalDutyPeriodProfile>()
    for (item in items) {
        val derived = newRegionalDutyPeriodProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRegionalDutyPeriodProfile(item: RegionalDutyPeriodProfile): String {
    return classifySwapTier(item.adjustedTotal())
}
