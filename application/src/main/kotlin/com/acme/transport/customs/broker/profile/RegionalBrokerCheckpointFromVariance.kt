package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.broker.exception.RegionalBrokerVariance
import com.acme.transport.customs.broker.exception.normalizeRegionalBrokerVarianceCode

fun summarizeRegionalBrokerVariancesForCheckpoint(items: MutableList<RegionalBrokerVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalBrokerCheckpointsFromVariance(items: MutableList<RegionalBrokerVariance>): MutableList<RegionalBrokerCheckpoint> {
    val out = mutableListOf<RegionalBrokerCheckpoint>()
    for (item in items) {
        val derived = newRegionalBrokerCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRegionalBrokerCheckpoint(item: RegionalBrokerCheckpoint): String {
    return normalizeRegionalBrokerVarianceCode(item.code)
}
