package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.terminal.crossdock.segment.CrossDockRequest
import com.acme.transport.terminal.crossdock.segment.classifyScheduledCrossDockRecord

fun summarizeCrossDockRequestsForVariance(items: MutableList<CrossDockRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveCrossDockVariancesFromRequest(items: MutableList<CrossDockRequest>): MutableList<CrossDockVariance> {
    val out = mutableListOf<CrossDockVariance>()
    for (item in items) {
        val derived = newCrossDockVariance(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCrossDockVariance(item: CrossDockVariance): String = classifyScheduledCrossDockRecord(item.netVolume())
