package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.terminal.dockdoor.target.DockDoorTarget
import com.acme.transport.terminal.dockdoor.target.classifyDockDoorTarget

fun summarizeDockDoorTargetsForSegment(items: MutableList<DockDoorTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerYardSegmentsFromTarget(items: MutableList<DockDoorTarget>): MutableList<TrailerYardSegment> {
    val out = mutableListOf<TrailerYardSegment>()
    for (item in items) {
        val derived = newTrailerYardSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTrailerYardSegment(item: TrailerYardSegment): String = classifyDockDoorTarget(item.adjustedTotal())
