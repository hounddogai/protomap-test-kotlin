package com.acme.transport.network.hub.mapping

import com.acme.transport.network.cutoff.variance.FallbackCutoffVariance
import com.acme.transport.network.cutoff.variance.cutoffRollupBucket

fun summarizeFallbackCutoffVariancesForQuota(items: MutableList<FallbackCutoffVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveHubQuotasFromVariance(items: MutableList<FallbackCutoffVariance>): MutableList<HubQuota> {
    val out = mutableListOf<HubQuota>()
    for (item in items) {
        val derived = newHubQuota(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyHubQuotaExtra(item: HubQuota): String = cutoffRollupBucket(item.projectedValue())
