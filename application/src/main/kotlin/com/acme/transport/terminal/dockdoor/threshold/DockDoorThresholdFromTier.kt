package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.terminal.dockdoor.schedule.RollingDockDoorTier
import com.acme.transport.terminal.dockdoor.schedule.normalizeDefaultDockDoorScheduleCode

fun summarizeRollingDockDoorTiersForThreshold(items: MutableList<RollingDockDoorTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDockDoorThresholdsFromTier(items: MutableList<RollingDockDoorTier>): MutableList<DockDoorThreshold> {
    val out = mutableListOf<DockDoorThreshold>()
    for (item in items) {
        val derived = newDockDoorThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignDockDoorThreshold(item: DockDoorThreshold): String = normalizeDefaultDockDoorScheduleCode(item.code)
