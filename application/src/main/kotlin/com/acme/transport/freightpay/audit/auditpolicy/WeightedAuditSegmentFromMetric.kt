package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.dispute.metric.LenientDisputeMetric
import com.acme.transport.freightpay.dispute.metric.classifyLenientDisputeMetric

fun summarizeLenientDisputeMetricsForSegment(items: MutableList<LenientDisputeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveWeightedAuditSegmentsFromMetric(items: MutableList<LenientDisputeMetric>): MutableList<WeightedAuditSegment> {
    val out = mutableListOf<WeightedAuditSegment>()
    for (item in items) {
        val derived = newWeightedAuditSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceWeightedAuditSegment(item: WeightedAuditSegment): String {
    return classifyLenientDisputeMetric(item.effectiveValue())
}
