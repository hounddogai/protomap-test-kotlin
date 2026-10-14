package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.terminal.dwell.plan.RegionalDwellPlan
import com.acme.transport.terminal.dwell.plan.dwellWindowWeight

fun summarizeRegionalDwellPlansForPreference(items: MutableList<RegionalDwellPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientCrossDockPreferencesFromPlan(items: MutableList<RegionalDwellPlan>): MutableList<LenientCrossDockPreference> {
    val out = mutableListOf<LenientCrossDockPreference>()
    for (item in items) {
        val derived = newLenientCrossDockPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankLenientCrossDockPreference(item: LenientCrossDockPreference): Double = dwellWindowWeight(3, true)
