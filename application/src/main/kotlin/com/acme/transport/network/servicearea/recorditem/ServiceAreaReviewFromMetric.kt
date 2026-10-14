package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.node.weightednodeagreement.AdaptiveNodeMetric
import com.acme.transport.network.node.weightednodeagreement.nodeRecordBucket

fun summarizeAdaptiveNodeMetricsForReview(items: MutableList<AdaptiveNodeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveServiceAreaReviewsFromMetric(items: MutableList<AdaptiveNodeMetric>): MutableList<ServiceAreaReview> {
    val out = mutableListOf<ServiceAreaReview>()
    for (item in items) {
        val derived = newServiceAreaReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyServiceAreaReview(item: ServiceAreaReview): String = nodeRecordBucket(item.effectiveExposure())
