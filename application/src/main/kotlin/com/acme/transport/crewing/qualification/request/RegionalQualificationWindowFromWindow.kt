package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.roster.entry.RosterWindow
import com.acme.transport.crewing.roster.entry.classifyTieredRosterException

fun summarizeRosterWindowsForWindow(items: MutableList<RosterWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalQualificationWindowsFromWindow(items: MutableList<RosterWindow>): MutableList<RegionalQualificationWindow> {
    val out = mutableListOf<RegionalQualificationWindow>()
    for (item in items) {
        val derived = newRegionalQualificationWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRegionalQualificationWindow(item: RegionalQualificationWindow): String {
    return classifyTieredRosterException(item.weightedTotal())
}
