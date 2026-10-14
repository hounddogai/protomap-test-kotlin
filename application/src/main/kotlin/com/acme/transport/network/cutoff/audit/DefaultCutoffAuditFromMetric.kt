package com.acme.transport.network.cutoff.audit

import com.acme.transport.network.node.metric.FallbackNodeMetric
import com.acme.transport.network.node.metric.nodeAuditWeight

fun summarizeFallbackNodeMetricsForAudit(items: MutableList<FallbackNodeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveDefaultCutoffAuditsFromMetric(items: MutableList<FallbackNodeMetric>): MutableList<DefaultCutoffAudit> {
    val out = mutableListOf<DefaultCutoffAudit>()
    for (item in items) {
        val derived = newDefaultCutoffAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDefaultCutoffAudit(item: DefaultCutoffAudit): Double = nodeAuditWeight(3, true)
