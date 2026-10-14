package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.declaration.schedule.SeasonalDeclarationSchedule
import com.acme.transport.customs.declaration.schedule.seasonalDeclarationScheduleBucket

fun summarizeSeasonalDeclarationSchedulesForOverride(items: MutableList<SeasonalDeclarationSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRollingDeclarationOverridesFromSchedule(items: MutableList<SeasonalDeclarationSchedule>): MutableList<RollingDeclarationOverride> {
    val out = mutableListOf<RollingDeclarationOverride>()
    for (item in items) {
        val derived = newRollingDeclarationOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRollingDeclarationOverride(item: RollingDeclarationOverride): String {
    return seasonalDeclarationScheduleBucket(item.weightedVolume())
}
