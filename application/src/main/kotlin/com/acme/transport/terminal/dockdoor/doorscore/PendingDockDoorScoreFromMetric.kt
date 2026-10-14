package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.terminal.sortation.template.SortationMetric
import com.acme.transport.terminal.sortation.template.sortationMetricWeight

fun summarizeSortationMetricsForScore(items: MutableList<SortationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePendingDockDoorScoresFromMetric(items: MutableList<SortationMetric>): MutableList<PendingDockDoorScore> {
    val out = mutableListOf<PendingDockDoorScore>()
    for (item in items) {
        val derived = newPendingDockDoorScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingDockDoorScore(item: PendingDockDoorScore): Double = sortationMetricWeight(9, true)
