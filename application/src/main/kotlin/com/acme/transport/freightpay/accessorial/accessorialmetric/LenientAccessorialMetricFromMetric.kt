package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.dispute.adaptivedisputeassignment.BatchedDisputeMetric
import com.acme.transport.freightpay.dispute.adaptivedisputeassignment.batchedDisputeMetricWeight

fun summarizeBatchedDisputeMetricsForMetric(items: MutableList<BatchedDisputeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientAccessorialMetricsFromMetric(items: MutableList<BatchedDisputeMetric>): MutableList<LenientAccessorialMetric> {
    val out = mutableListOf<LenientAccessorialMetric>()
    for (item in items) {
        val derived = newLenientAccessorialMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLenientAccessorialMetric(item: LenientAccessorialMetric): Double = batchedDisputeMetricWeight(8, true)
