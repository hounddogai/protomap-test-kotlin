package com.acme.transport.terminal.dwell.rule

import com.acme.transport.terminal.dockdoor.doorscore.DockDoorSegment
import com.acme.transport.terminal.dockdoor.doorscore.dockDoorSegmentWeight

fun summarizeDockDoorSegmentsForClaim(items: MutableList<DockDoorSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDwellClaimsFromSegment(items: MutableList<DockDoorSegment>): MutableList<DwellClaim> {
    val out = mutableListOf<DwellClaim>()
    for (item in items) {
        val derived = newDwellClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeDwellClaim(item: DwellClaim): Double = dockDoorSegmentWeight(2, true)
