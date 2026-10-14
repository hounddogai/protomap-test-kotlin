package com.acme.transport.linehaul.tender.group

import com.acme.transport.linehaul.trailer.regionaltrailerrecord.DefaultTrailerMetric
import com.acme.transport.linehaul.trailer.regionaltrailerrecord.manualTrailerTargetWeight

fun summarizeDefaultTrailerMetricsForProfile(items: MutableList<DefaultTrailerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictTenderProfilesFromMetric(items: MutableList<DefaultTrailerMetric>): MutableList<StrictTenderProfile> {
    val out = mutableListOf<StrictTenderProfile>()
    for (item in items) {
        val derived = newStrictTenderProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileStrictTenderProfile(item: StrictTenderProfile): Double = manualTrailerTargetWeight(5, true)
