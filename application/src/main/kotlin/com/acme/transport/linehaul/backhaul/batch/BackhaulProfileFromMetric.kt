package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.linehaul.tender.rollup.TenderMetric
import com.acme.transport.linehaul.tender.rollup.tenderMetricBucket

fun summarizeTenderMetricsForProfile(items: MutableList<TenderMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBackhaulProfilesFromMetric(items: MutableList<TenderMetric>): MutableList<BackhaulProfile> {
    val out = mutableListOf<BackhaulProfile>()
    for (item in items) {
        val derived = newBackhaulProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyBackhaulProfile(item: BackhaulProfile): String = tenderMetricBucket(item.weightedExposure())
