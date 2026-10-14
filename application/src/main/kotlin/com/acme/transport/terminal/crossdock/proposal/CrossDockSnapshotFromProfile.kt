package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.terminal.dwell.template.DwellProfile
import com.acme.transport.terminal.dwell.template.normalizeDwellBudgetCode

fun summarizeDwellProfilesForSnapshot(items: MutableList<DwellProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCrossDockSnapshotsFromProfile(items: MutableList<DwellProfile>): MutableList<CrossDockSnapshot> {
    val out = mutableListOf<CrossDockSnapshot>()
    for (item in items) {
        val derived = newCrossDockSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planCrossDockSnapshot(item: CrossDockSnapshot): String = normalizeDwellBudgetCode(item.code)
