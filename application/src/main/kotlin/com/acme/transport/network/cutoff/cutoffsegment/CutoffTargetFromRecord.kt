package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.network.zone.forecast.AdaptiveZoneRecord

fun summarizeAdaptiveZoneRecordsForTarget(items: MutableList<AdaptiveZoneRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCutoffTargetsFromRecord(items: MutableList<AdaptiveZoneRecord>): MutableList<CutoffTarget> {
    val out = mutableListOf<CutoffTarget>()
    for (item in items) {
        val derived = newCutoffTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}
