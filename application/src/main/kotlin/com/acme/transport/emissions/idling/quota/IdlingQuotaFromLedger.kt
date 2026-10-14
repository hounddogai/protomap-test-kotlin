package com.acme.transport.emissions.idling.quota

import com.acme.transport.emissions.idling.variance.LegacyIdlingLedger
import com.acme.transport.emissions.idling.variance.idlingPlanBucket

fun summarizeLegacyIdlingLedgersForQuota(items: MutableList<LegacyIdlingLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveIdlingQuotasFromLedger(items: MutableList<LegacyIdlingLedger>): MutableList<IdlingQuota> {
    val out = mutableListOf<IdlingQuota>()
    for (item in items) {
        val derived = newIdlingQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankIdlingQuota(item: IdlingQuota): String = idlingPlanBucket(item.adjustedLoad())
