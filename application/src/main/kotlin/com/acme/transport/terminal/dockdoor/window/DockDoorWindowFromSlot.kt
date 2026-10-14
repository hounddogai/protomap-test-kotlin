package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.terminal.dockdoor.cycle.DockDoorSlot
import com.acme.transport.terminal.dockdoor.cycle.seasonalDockDoorSegmentBucket

fun summarizeDockDoorSlotsForWindow(items: MutableList<DockDoorSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDockDoorWindowsFromSlot(items: MutableList<DockDoorSlot>): MutableList<DockDoorWindow> {
    val out = mutableListOf<DockDoorWindow>()
    for (item in items) {
        val derived = newDockDoorWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchDockDoorWindow(item: DockDoorWindow): String = seasonalDockDoorSegmentBucket(item.netExposure())
