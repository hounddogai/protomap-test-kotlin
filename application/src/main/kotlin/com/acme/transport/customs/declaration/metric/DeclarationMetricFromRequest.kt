package com.acme.transport.customs.declaration.metric

import com.acme.transport.customs.hold.baseline.HoldRequest

fun summarizeHoldRequestsForMetric(items: MutableList<HoldRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDeclarationMetricsFromRequest(items: MutableList<HoldRequest>): MutableList<DeclarationMetric> {
    val out = mutableListOf<DeclarationMetric>()
    for (item in items) {
        val derived = newDeclarationMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}
