package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.declaration.metric.DeclarationMetric
import com.acme.transport.customs.declaration.metric.declarationMetricBucket

fun summarizeDeclarationMetricsForSummary(items: MutableList<DeclarationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBrokerSummariesFromMetric(items: MutableList<DeclarationMetric>): MutableList<BrokerSummary> {
    val out = mutableListOf<BrokerSummary>()
    for (item in items) {
        val derived = newBrokerSummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveBrokerSummary(item: BrokerSummary): String = declarationMetricBucket(item.netExposure())
