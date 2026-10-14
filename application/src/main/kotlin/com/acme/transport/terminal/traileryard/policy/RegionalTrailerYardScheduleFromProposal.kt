package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.terminal.dockdoor.window.RollingDockDoorProposal
import com.acme.transport.terminal.dockdoor.window.dockDoorBaselineBucket

fun summarizeRollingDockDoorProposalsForSchedule(items: MutableList<RollingDockDoorProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRegionalTrailerYardSchedulesFromProposal(items: MutableList<RollingDockDoorProposal>): MutableList<RegionalTrailerYardSchedule> {
    val out = mutableListOf<RegionalTrailerYardSchedule>()
    for (item in items) {
        val derived = newRegionalTrailerYardSchedule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalTrailerYardSchedule(item: RegionalTrailerYardSchedule): String {
    return dockDoorBaselineBucket(item.effectiveTotal())
}
