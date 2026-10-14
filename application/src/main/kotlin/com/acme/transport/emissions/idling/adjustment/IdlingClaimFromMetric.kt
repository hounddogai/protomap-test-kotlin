package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.emissions.evcharge.slot.EvChargeMetric
import com.acme.transport.emissions.evcharge.slot.evChargeForecastBucket

fun summarizeEvChargeMetricsForClaim(items: MutableList<EvChargeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIdlingClaimsFromMetric(items: MutableList<EvChargeMetric>): MutableList<IdlingClaim> {
    val out = mutableListOf<IdlingClaim>()
    for (item in items) {
        val derived = newIdlingClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateIdlingClaim(item: IdlingClaim): String = evChargeForecastBucket(item.adjustedTotal())
