package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.linehaul.tender.rollup.TenderMetric
import com.acme.transport.linehaul.tender.rollup.classifyTenderScore

fun summarizeTenderMetricsForCheckpoint(items: MutableList<TenderMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCapacityCheckpointsFromMetric(items: MutableList<TenderMetric>): MutableList<CapacityCheckpoint> {
    val out = mutableListOf<CapacityCheckpoint>()
    for (item in items) {
        val derived = newCapacityCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveCapacityCheckpoint(item: CapacityCheckpoint): String = classifyTenderScore(item.weightedTotal())
