package com.acme.transport.linehaul.tender.metric

import com.acme.transport.linehaul.tender.tenderestimate.StrictTenderEstimate
import com.acme.transport.linehaul.tender.tenderestimate.classifyPendingTenderMetric

fun summarizeStrictTenderEstimatesForMetric(items: MutableList<StrictTenderEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultTenderMetricsFromEstimate(items: MutableList<StrictTenderEstimate>): MutableList<DefaultTenderMetric> {
    val out = mutableListOf<DefaultTenderMetric>()
    for (item in items) {
        val derived = newDefaultTenderMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultTenderMetric(item: DefaultTenderMetric): String {
    return classifyPendingTenderMetric(item.adjustedValue())
}
