package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.terminal.crossdock.dockslot.CrossDockAdjustment
import com.acme.transport.terminal.crossdock.dockslot.cappedCrossDockRollupWeight

fun summarizeCrossDockAdjustmentsForBaseline(items: MutableList<CrossDockAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedDockDoorBaselinesFromAdjustment(items: MutableList<CrossDockAdjustment>): MutableList<CappedDockDoorBaseline> {
    val out = mutableListOf<CappedDockDoorBaseline>()
    for (item in items) {
        val derived = newCappedDockDoorBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeCappedDockDoorBaseline(item: CappedDockDoorBaseline): Double = cappedCrossDockRollupWeight(3, true)
