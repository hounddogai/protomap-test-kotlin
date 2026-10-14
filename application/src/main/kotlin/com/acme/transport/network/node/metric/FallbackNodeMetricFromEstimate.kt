package com.acme.transport.network.node.metric

import com.acme.transport.network.cutoff.template.DefaultCutoffEstimate
import com.acme.transport.network.cutoff.template.normalizeLenientCutoffTemplateCode

fun summarizeDefaultCutoffEstimatesForMetric(items: MutableList<DefaultCutoffEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveFallbackNodeMetricsFromEstimate(items: MutableList<DefaultCutoffEstimate>): MutableList<FallbackNodeMetric> {
    val out = mutableListOf<FallbackNodeMetric>()
    for (item in items) {
        val derived = newFallbackNodeMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFallbackNodeMetric(item: FallbackNodeMetric): String = normalizeLenientCutoffTemplateCode(item.code)
