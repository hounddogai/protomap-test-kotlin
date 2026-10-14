package com.acme.transport.network.cutoff.summary

import com.acme.transport.network.servicearea.exception.RegionalServiceAreaThreshold
import com.acme.transport.network.servicearea.exception.normalizeRegionalServiceAreaThresholdCode

fun summarizeRegionalServiceAreaThresholdsForRequest(items: MutableList<RegionalServiceAreaThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveCutoffRequestsFromThreshold(items: MutableList<RegionalServiceAreaThreshold>): MutableList<CutoffRequest> {
    val out = mutableListOf<CutoffRequest>()
    for (item in items) {
        val derived = newCutoffRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleCutoffRequest(item: CutoffRequest): String = normalizeRegionalServiceAreaThresholdCode(item.code)
