package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.terminal.dwell.cycle.DwellEntry
import com.acme.transport.terminal.dwell.cycle.dwellAssignmentWeight

fun summarizeDwellEntriesForSegment(items: MutableList<DwellEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDockDoorSegmentsFromEntry(items: MutableList<DwellEntry>): MutableList<DockDoorSegment> {
    val out = mutableListOf<DockDoorSegment>()
    for (item in items) {
        val derived = newDockDoorSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankDockDoorSegment(item: DockDoorSegment): Double = dwellAssignmentWeight(8, true)
