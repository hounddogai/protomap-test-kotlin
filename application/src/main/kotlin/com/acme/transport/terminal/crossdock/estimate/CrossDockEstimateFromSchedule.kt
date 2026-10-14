package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.terminal.crossdock.recorditem.CrossDockSchedule
import com.acme.transport.terminal.crossdock.recorditem.classifyCrossDockRecord

fun summarizeCrossDockSchedulesForEstimate(items: MutableList<CrossDockSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCrossDockEstimatesFromSchedule(items: MutableList<CrossDockSchedule>): MutableList<CrossDockEstimate> {
    val out = mutableListOf<CrossDockEstimate>()
    for (item in items) {
        val derived = newCrossDockEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeCrossDockEstimate(item: CrossDockEstimate): String = classifyCrossDockRecord(item.weightedLoad())
