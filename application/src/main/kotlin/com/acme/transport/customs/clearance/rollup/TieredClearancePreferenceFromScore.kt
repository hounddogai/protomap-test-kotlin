package com.acme.transport.customs.clearance.rollup

import com.acme.transport.customs.broker.brokergroup.TieredBrokerScore
import com.acme.transport.customs.broker.brokergroup.weightedBrokerGroupBucket

fun summarizeTieredBrokerScoresForPreference(items: MutableList<TieredBrokerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTieredClearancePreferencesFromScore(items: MutableList<TieredBrokerScore>): MutableList<TieredClearancePreference> {
    val out = mutableListOf<TieredClearancePreference>()
    for (item in items) {
        val derived = newTieredClearancePreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceTieredClearancePreference(item: TieredClearancePreference): String {
    return weightedBrokerGroupBucket(item.adjustedValue())
}
