package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.linehaul.laneplan.window.WeightedLanePlanWindow
import com.acme.transport.linehaul.laneplan.window.fallbackLanePlanExceptionBucket

fun summarizeWeightedLanePlanWindowsForRecord(items: MutableList<WeightedLanePlanWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveTrailerRecordsFromWindow(items: MutableList<WeightedLanePlanWindow>): MutableList<TrailerRecord> {
    val out = mutableListOf<TrailerRecord>()
    for (item in items) {
        val derived = newTrailerRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateTrailerRecord(item: TrailerRecord): String = fallbackLanePlanExceptionBucket(item.effectiveExposure())
