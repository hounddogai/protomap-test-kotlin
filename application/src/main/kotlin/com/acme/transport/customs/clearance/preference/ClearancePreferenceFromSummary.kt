package com.acme.transport.customs.clearance.preference

import com.acme.transport.customs.broker.allocation.SeasonalBrokerSummary
import com.acme.transport.customs.broker.allocation.globalBrokerAdjustmentBucket

fun summarizeSeasonalBrokerSummariesForPreference(items: MutableList<SeasonalBrokerSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveClearancePreferencesFromSummary(items: MutableList<SeasonalBrokerSummary>): MutableList<ClearancePreference> {
    val out = mutableListOf<ClearancePreference>()
    for (item in items) {
        val derived = newClearancePreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleClearancePreference(item: ClearancePreference): String {
    return globalBrokerAdjustmentBucket(item.effectiveTotal())
}
