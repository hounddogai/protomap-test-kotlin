package com.acme.transport.terminal.traileryard.template

import com.acme.transport.terminal.crossdock.target.CrossDockCheckpoint
import com.acme.transport.terminal.crossdock.target.normalizeCrossDockTargetCode

fun summarizeCrossDockCheckpointsForTemplate(items: MutableList<CrossDockCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTrailerYardTemplatesFromCheckpoint(items: MutableList<CrossDockCheckpoint>): MutableList<TrailerYardTemplate> {
    val out = mutableListOf<TrailerYardTemplate>()
    for (item in items) {
        val derived = newTrailerYardTemplate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTrailerYardTemplate(item: TrailerYardTemplate): String = normalizeCrossDockTargetCode(item.code)
