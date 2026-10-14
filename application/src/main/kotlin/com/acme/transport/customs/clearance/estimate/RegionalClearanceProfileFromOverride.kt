package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.clearance.primaryclearanceoverride.RegionalClearanceOverride
import com.acme.transport.customs.clearance.primaryclearanceoverride.normalizeStrictClearanceScheduleCode

fun summarizeRegionalClearanceOverridesForProfile(items: MutableList<RegionalClearanceOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRegionalClearanceProfilesFromOverride(items: MutableList<RegionalClearanceOverride>): MutableList<RegionalClearanceProfile> {
    val out = mutableListOf<RegionalClearanceProfile>()
    for (item in items) {
        val derived = newRegionalClearanceProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRegionalClearanceProfile(item: RegionalClearanceProfile): String {
    return normalizeStrictClearanceScheduleCode(item.code)
}
