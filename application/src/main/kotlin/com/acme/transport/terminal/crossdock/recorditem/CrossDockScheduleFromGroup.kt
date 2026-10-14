package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.terminal.dockdoor.ledger.DockDoorGroup
import com.acme.transport.terminal.dockdoor.ledger.classifyDockDoorPlan

fun summarizeDockDoorGroupsForSchedule(items: MutableList<DockDoorGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockSchedulesFromGroup(items: MutableList<DockDoorGroup>): MutableList<CrossDockSchedule> {
    val out = mutableListOf<CrossDockSchedule>()
    for (item in items) {
        val derived = newCrossDockSchedule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreCrossDockSchedule(item: CrossDockSchedule): String = classifyDockDoorPlan(item.weightedVolume())
