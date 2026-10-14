package com.acme.transport.customs.clearance.claim

import com.acme.transport.customs.hold.forecast.PrimaryHoldAdjustment
import com.acme.transport.customs.hold.forecast.classifyPrimaryHoldNotice

fun summarizePrimaryHoldAdjustmentsForOverride(items: MutableList<PrimaryHoldAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalClearanceOverridesFromAdjustment(items: MutableList<PrimaryHoldAdjustment>): MutableList<SeasonalClearanceOverride> {
    val out = mutableListOf<SeasonalClearanceOverride>()
    for (item in items) {
        val derived = newSeasonalClearanceOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applySeasonalClearanceOverride(item: SeasonalClearanceOverride): String {
    return classifyPrimaryHoldNotice(item.projectedVolume())
}
