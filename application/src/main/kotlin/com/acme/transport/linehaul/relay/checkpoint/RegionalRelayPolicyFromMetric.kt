package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.linehaul.capacity.metric.SeasonalCapacityMetric
import com.acme.transport.linehaul.capacity.metric.capacityProfileBucket

fun summarizeSeasonalCapacityMetricsForPolicy(items: MutableList<SeasonalCapacityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalRelayPoliciesFromMetric(items: MutableList<SeasonalCapacityMetric>): MutableList<RegionalRelayPolicy> {
    val out = mutableListOf<RegionalRelayPolicy>()
    for (item in items) {
        val derived = newRegionalRelayPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalRelayPolicy(item: RegionalRelayPolicy): String = capacityProfileBucket(item.measuredValue())
