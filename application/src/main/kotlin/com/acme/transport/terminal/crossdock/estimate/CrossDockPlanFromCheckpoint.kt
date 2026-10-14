package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.terminal.dwell.checkpoint.DwellCheckpoint
import com.acme.transport.terminal.dwell.checkpoint.dwellCheckpointWeight

fun summarizeDwellCheckpointsForPlan(items: MutableList<DwellCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCrossDockPlansFromCheckpoint(items: MutableList<DwellCheckpoint>): MutableList<CrossDockPlan> {
    val out = mutableListOf<CrossDockPlan>()
    for (item in items) {
        val derived = newCrossDockPlan(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCrossDockPlan(item: CrossDockPlan): Double = dwellCheckpointWeight(9, true)
