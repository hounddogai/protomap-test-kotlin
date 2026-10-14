package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.dockdoor.doorrecord.DefaultDockDoorTier
import com.acme.transport.terminal.dockdoor.doorrecord.normalizeStrictDockDoorWindowCode

fun summarizeDefaultDockDoorTiersForAllocation(items: MutableList<DefaultDockDoorTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveScheduledDwellAllocationsFromTier(items: MutableList<DefaultDockDoorTier>): MutableList<ScheduledDwellAllocation> {
    val out = mutableListOf<ScheduledDwellAllocation>()
    for (item in items) {
        val derived = newScheduledDwellAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyScheduledDwellAllocation(item: ScheduledDwellAllocation): String {
    return normalizeStrictDockDoorWindowCode(item.code)
}
