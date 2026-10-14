package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.accessorial.accessorialplan.AccessorialEstimate
import com.acme.transport.freightpay.accessorial.accessorialplan.seasonalAccessorialTierWeight

fun summarizeAccessorialEstimatesForEstimate(items: MutableList<AccessorialEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveBatchedDisputeEstimatesFromEstimate(items: MutableList<AccessorialEstimate>): MutableList<BatchedDisputeEstimate> {
    val out = mutableListOf<BatchedDisputeEstimate>()
    for (item in items) {
        val derived = newBatchedDisputeEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeBatchedDisputeEstimate(item: BatchedDisputeEstimate): Double = seasonalAccessorialTierWeight(7, true)
