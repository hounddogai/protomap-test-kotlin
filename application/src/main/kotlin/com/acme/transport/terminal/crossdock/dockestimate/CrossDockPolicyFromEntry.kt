package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.terminal.dwell.schedule.CappedDwellEntry
import com.acme.transport.terminal.dwell.schedule.scheduledDwellScheduleWeight

fun summarizeCappedDwellEntriesForPolicy(items: MutableList<CappedDwellEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCrossDockPoliciesFromEntry(items: MutableList<CappedDwellEntry>): MutableList<CrossDockPolicy> {
    val out = mutableListOf<CrossDockPolicy>()
    for (item in items) {
        val derived = newCrossDockPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCrossDockPolicy(item: CrossDockPolicy): Double = scheduledDwellScheduleWeight(6, true)
