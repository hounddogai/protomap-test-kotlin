package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.terminal.dwell.notice.LenientDwellTemplate
import com.acme.transport.terminal.dwell.notice.dwellScoreWeight

fun summarizeLenientDwellTemplatesForAllocation(items: MutableList<LenientDwellTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockAllocationsFromTemplate(items: MutableList<LenientDwellTemplate>): MutableList<CrossDockAllocation> {
    val out = mutableListOf<CrossDockAllocation>()
    for (item in items) {
        val derived = newCrossDockAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveCrossDockAllocation(item: CrossDockAllocation): Double = dwellScoreWeight(4, true)
