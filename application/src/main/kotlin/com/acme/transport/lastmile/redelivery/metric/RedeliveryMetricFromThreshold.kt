package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.lastmile.proofofdelivery.target.ProofOfDeliveryThreshold
import com.acme.transport.lastmile.proofofdelivery.target.classifyProofOfDeliveryNotice

fun summarizeProofOfDeliveryThresholdsForMetric(items: MutableList<ProofOfDeliveryThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryMetricsFromThreshold(items: MutableList<ProofOfDeliveryThreshold>): MutableList<RedeliveryMetric> {
    val out = mutableListOf<RedeliveryMetric>()
    for (item in items) {
        val derived = newRedeliveryMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRedeliveryMetric(item: RedeliveryMetric): String = classifyProofOfDeliveryNotice(item.netVolume())
