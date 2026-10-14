package com.acme.transport.linehaul.relay.variance

import com.acme.transport.linehaul.trailer.budget.FallbackTrailerOverride
import com.acme.transport.linehaul.trailer.budget.fallbackTrailerOverrideWeight

fun summarizeFallbackTrailerOverridesForVariance(items: MutableList<FallbackTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDefaultRelayVariancesFromOverride(items: MutableList<FallbackTrailerOverride>): MutableList<DefaultRelayVariance> {
    val out = mutableListOf<DefaultRelayVariance>()
    for (item in items) {
        val derived = newDefaultRelayVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultRelayVariance(item: DefaultRelayVariance): Double = fallbackTrailerOverrideWeight(5, true)
