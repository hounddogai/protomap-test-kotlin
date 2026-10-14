package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.idling.idlingmetric.LegacyIdlingMetric
import com.acme.transport.emissions.idling.idlingmetric.legacyIdlingMetricBucket

fun summarizeLegacyIdlingMetricsForPolicy(items: MutableList<LegacyIdlingMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveEvChargePoliciesFromMetric(items: MutableList<LegacyIdlingMetric>): MutableList<EvChargePolicy> {
    val out = mutableListOf<EvChargePolicy>()
    for (item in items) {
        val derived = newEvChargePolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateEvChargePolicy(item: EvChargePolicy): String = legacyIdlingMetricBucket(item.netVolume())
