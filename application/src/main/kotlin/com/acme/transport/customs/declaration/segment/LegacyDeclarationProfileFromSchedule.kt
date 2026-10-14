package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clearance.schedule.FallbackClearanceSchedule
import com.acme.transport.customs.clearance.schedule.fallbackClearanceScheduleWeight

fun summarizeFallbackClearanceSchedulesForProfile(items: MutableList<FallbackClearanceSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLegacyDeclarationProfilesFromSchedule(items: MutableList<FallbackClearanceSchedule>): MutableList<LegacyDeclarationProfile> {
    val out = mutableListOf<LegacyDeclarationProfile>()
    for (item in items) {
        val derived = newLegacyDeclarationProfile(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyDeclarationProfile(item: LegacyDeclarationProfile): Double {
    return fallbackClearanceScheduleWeight(3, true)
}
