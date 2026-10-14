package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.restbreak.legacybreakentry.RegionalRestBreakAdjustment
import com.acme.transport.crewing.restbreak.legacybreakentry.rollingRestBreakMilestoneBucket

fun summarizeRegionalRestBreakAdjustmentsForPreference(items: MutableList<RegionalRestBreakAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCappedAvailabilityPreferencesFromAdjustment(items: MutableList<RegionalRestBreakAdjustment>): MutableList<CappedAvailabilityPreference> {
    val out = mutableListOf<CappedAvailabilityPreference>()
    for (item in items) {
        val derived = newCappedAvailabilityPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCappedAvailabilityPreference(item: CappedAvailabilityPreference): String {
    return rollingRestBreakMilestoneBucket(item.measuredValue())
}
