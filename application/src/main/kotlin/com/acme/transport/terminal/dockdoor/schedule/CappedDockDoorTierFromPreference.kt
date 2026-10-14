package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.terminal.dwell.dwellpreference.DefaultDwellPreference
import com.acme.transport.terminal.dwell.dwellpreference.classifyDefaultDwellPreference

fun summarizeDefaultDwellPreferencesForTier(items: MutableList<DefaultDwellPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveCappedDockDoorTiersFromPreference(items: MutableList<DefaultDwellPreference>): MutableList<CappedDockDoorTier> {
    val out = mutableListOf<CappedDockDoorTier>()
    for (item in items) {
        val derived = newCappedDockDoorTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleCappedDockDoorTier(item: CappedDockDoorTier): String {
    return classifyDefaultDwellPreference(item.weightedExposure())
}
