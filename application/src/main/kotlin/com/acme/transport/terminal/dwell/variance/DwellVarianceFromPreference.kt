package com.acme.transport.terminal.dwell.variance

import com.acme.transport.terminal.crossdock.exception.CrossDockPreference
import com.acme.transport.terminal.crossdock.exception.normalizeLegacyCrossDockAssignmentCode

fun summarizeCrossDockPreferencesForVariance(items: MutableList<CrossDockPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDwellVariancesFromPreference(items: MutableList<CrossDockPreference>): MutableList<DwellVariance> {
    val out = mutableListOf<DwellVariance>()
    for (item in items) {
        val derived = newDwellVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDwellVarianceExtra(item: DwellVariance): String = normalizeLegacyCrossDockAssignmentCode(item.code)
