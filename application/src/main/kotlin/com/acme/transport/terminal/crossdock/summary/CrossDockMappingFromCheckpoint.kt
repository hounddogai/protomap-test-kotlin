package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.terminal.sortation.sortationbaseline.PendingSortationCheckpoint
import com.acme.transport.terminal.sortation.sortationbaseline.classifyRegionalSortationClaim

fun summarizePendingSortationCheckpointsForMapping(items: MutableList<PendingSortationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockMappingsFromCheckpoint(items: MutableList<PendingSortationCheckpoint>): MutableList<CrossDockMapping> {
    val out = mutableListOf<CrossDockMapping>()
    for (item in items) {
        val derived = newCrossDockMapping(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeCrossDockMapping(item: CrossDockMapping): String = classifyRegionalSortationClaim(item.netValue())
