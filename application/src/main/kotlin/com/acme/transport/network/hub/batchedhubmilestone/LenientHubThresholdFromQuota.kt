package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.network.servicearea.milestone.FallbackServiceAreaQuota
import com.acme.transport.network.servicearea.milestone.fallbackServiceAreaQuotaWeight

fun summarizeFallbackServiceAreaQuotasForThreshold(items: MutableList<FallbackServiceAreaQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLenientHubThresholdsFromQuota(items: MutableList<FallbackServiceAreaQuota>): MutableList<LenientHubThreshold> {
    val out = mutableListOf<LenientHubThreshold>()
    for (item in items) {
        val derived = newLenientHubThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLenientHubThreshold(item: LenientHubThreshold): Double = fallbackServiceAreaQuotaWeight(4, true)
