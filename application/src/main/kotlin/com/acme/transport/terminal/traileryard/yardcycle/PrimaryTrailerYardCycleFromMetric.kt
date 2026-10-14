package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.terminal.crossdock.schedule.CrossDockMetric
import com.acme.transport.terminal.crossdock.schedule.classifyCrossDockDigest

fun summarizeCrossDockMetricsForCycle(items: MutableList<CrossDockMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryTrailerYardCyclesFromMetric(items: MutableList<CrossDockMetric>): MutableList<PrimaryTrailerYardCycle> {
    val out = mutableListOf<PrimaryTrailerYardCycle>()
    for (item in items) {
        val derived = newPrimaryTrailerYardCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryTrailerYardCycle(item: PrimaryTrailerYardCycle): String {
    return classifyCrossDockDigest(item.effectiveTotal())
}
