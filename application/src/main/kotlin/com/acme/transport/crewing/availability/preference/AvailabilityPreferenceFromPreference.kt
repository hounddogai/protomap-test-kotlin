package com.acme.transport.crewing.availability.preference

import com.acme.transport.crewing.restbreak.preference.RestBreakPreference
import com.acme.transport.crewing.restbreak.preference.normalizeRestBreakPreferenceCode

fun summarizeRestBreakPreferencesForPreference(items: MutableList<RestBreakPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAvailabilityPreferencesFromPreference(items: MutableList<RestBreakPreference>): MutableList<AvailabilityPreference> {
    val out = mutableListOf<AvailabilityPreference>()
    for (item in items) {
        val derived = newAvailabilityPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeAvailabilityPreference(item: AvailabilityPreference): String {
    return normalizeRestBreakPreferenceCode(item.code)
}
