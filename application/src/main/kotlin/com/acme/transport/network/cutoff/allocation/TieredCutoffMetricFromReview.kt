package com.acme.transport.network.cutoff.allocation

import com.acme.transport.network.node.schedule.NodeReview
import com.acme.transport.network.node.schedule.nodeScheduleWeight

fun summarizeNodeReviewsForMetric(items: MutableList<NodeReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredCutoffMetricsFromReview(items: MutableList<NodeReview>): MutableList<TieredCutoffMetric> {
    val out = mutableListOf<TieredCutoffMetric>()
    for (item in items) {
        val derived = newTieredCutoffMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateTieredCutoffMetric(item: TieredCutoffMetric): Double = nodeScheduleWeight(4, true)
