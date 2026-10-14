package com.acme.transport.network.transittime.claim

import com.acme.transport.network.servicearea.estimate.ServiceAreaEstimate
import com.acme.transport.network.servicearea.estimate.normalizeServiceAreaEstimateCode

fun summarizeServiceAreaEstimatesForNotice(items: MutableList<ServiceAreaEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCappedTransitTimeNoticesFromEstimate(items: MutableList<ServiceAreaEstimate>): MutableList<CappedTransitTimeNotice> {
    val out = mutableListOf<CappedTransitTimeNotice>()
    for (item in items) {
        val derived = newCappedTransitTimeNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCappedTransitTimeNotice(item: CappedTransitTimeNotice): String {
    return normalizeServiceAreaEstimateCode(item.code)
}
