package com.acme.transport.network.transittime.budget

import com.acme.transport.network.node.nodesnapshot.LegacyNodeMetric
import com.acme.transport.network.node.nodesnapshot.regionalNodeSnapshotBucket

fun summarizeLegacyNodeMetricsForRule(items: MutableList<LegacyNodeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTransitTimeRulesFromMetric(items: MutableList<LegacyNodeMetric>): MutableList<TransitTimeRule> {
    val out = mutableListOf<TransitTimeRule>()
    for (item in items) {
        val derived = newTransitTimeRule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTransitTimeRule(item: TransitTimeRule): String = regionalNodeSnapshotBucket(item.adjustedExposure())
