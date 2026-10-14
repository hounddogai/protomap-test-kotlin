package com.acme.transport.terminal.sortation.digest

import com.acme.transport.terminal.dockdoor.mapping.DockDoorProfile
import com.acme.transport.terminal.dockdoor.mapping.normalizeDockDoorMappingCode

fun summarizeDockDoorProfilesForLedger(items: MutableList<DockDoorProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveScheduledSortationLedgersFromProfile(items: MutableList<DockDoorProfile>): MutableList<ScheduledSortationLedger> {
    val out = mutableListOf<ScheduledSortationLedger>()
    for (item in items) {
        val derived = newScheduledSortationLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectScheduledSortationLedger(item: ScheduledSortationLedger): String {
    return normalizeDockDoorMappingCode(item.code)
}
