package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.linehaul.tender.tenderrollup.ScheduledTenderCycle
import com.acme.transport.linehaul.tender.tenderrollup.classifyScheduledTenderCycle

fun summarizeScheduledTenderCyclesForMetric(items: MutableList<ScheduledTenderCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackBackhaulMetricsFromCycle(items: MutableList<ScheduledTenderCycle>): MutableList<FallbackBackhaulMetric> {
    val out = mutableListOf<FallbackBackhaulMetric>()
    for (item in items) {
        val derived = newFallbackBackhaulMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackBackhaulMetric(item: FallbackBackhaulMetric): String {
    return classifyScheduledTenderCycle(item.weightedTotal())
}
