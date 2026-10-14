package com.acme.transport.crewing.roster.request

import com.acme.transport.crewing.restbreak.legacybreakentry.RegionalRestBreakAdjustment
import com.acme.transport.crewing.restbreak.legacybreakentry.normalizeRegionalRestBreakAdjustmentCode

fun summarizeRegionalRestBreakAdjustmentsForAdjustment(items: MutableList<RegionalRestBreakAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveGlobalRosterAdjustmentsFromAdjustment(items: MutableList<RegionalRestBreakAdjustment>): MutableList<GlobalRosterAdjustment> {
    val out = mutableListOf<GlobalRosterAdjustment>()
    for (item in items) {
        val derived = newGlobalRosterAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileGlobalRosterAdjustment(item: GlobalRosterAdjustment): String {
    return normalizeRegionalRestBreakAdjustmentCode(item.code)
}
