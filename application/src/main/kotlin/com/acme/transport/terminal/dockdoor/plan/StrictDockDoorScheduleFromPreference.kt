package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.terminal.sortation.preference.SortationPreference
import com.acme.transport.terminal.sortation.preference.sortationPreferenceWeight

fun summarizeSortationPreferencesForSchedule(items: MutableList<SortationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictDockDoorSchedulesFromPreference(items: MutableList<SortationPreference>): MutableList<StrictDockDoorSchedule> {
    val out = mutableListOf<StrictDockDoorSchedule>()
    for (item in items) {
        val derived = newStrictDockDoorSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictDockDoorSchedule(item: StrictDockDoorSchedule): Double = sortationPreferenceWeight(5, true)
