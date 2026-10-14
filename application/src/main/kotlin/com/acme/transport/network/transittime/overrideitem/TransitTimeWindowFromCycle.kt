package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.network.node.score.PendingNodeCycle
import com.acme.transport.network.node.score.normalizeRollingNodeScoreCode

fun summarizePendingNodeCyclesForWindow(items: MutableList<PendingNodeCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTransitTimeWindowsFromCycle(items: MutableList<PendingNodeCycle>): MutableList<TransitTimeWindow> {
    val out = mutableListOf<TransitTimeWindow>()
    for (item in items) {
        val derived = newTransitTimeWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchTransitTimeWindow(item: TransitTimeWindow): String = normalizeRollingNodeScoreCode(item.code)
