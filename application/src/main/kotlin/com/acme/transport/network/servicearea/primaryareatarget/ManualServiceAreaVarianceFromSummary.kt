package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.network.hub.hubsummary.LenientHubSummary
import com.acme.transport.network.hub.hubsummary.normalizeLenientHubManifestCode

fun summarizeLenientHubSummariesForVariance(items: MutableList<LenientHubSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveManualServiceAreaVariancesFromSummary(items: MutableList<LenientHubSummary>): MutableList<ManualServiceAreaVariance> {
    val out = mutableListOf<ManualServiceAreaVariance>()
    for (item in items) {
        val derived = newManualServiceAreaVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeManualServiceAreaVariance(item: ManualServiceAreaVariance): String {
    return normalizeLenientHubManifestCode(item.code)
}
