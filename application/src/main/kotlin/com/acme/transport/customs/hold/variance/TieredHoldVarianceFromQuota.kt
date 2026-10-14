package com.acme.transport.customs.hold.variance

import com.acme.transport.customs.broker.batch.ManualBrokerQuota
import com.acme.transport.customs.broker.batch.manualBrokerQuotaWeight

fun summarizeManualBrokerQuotasForVariance(items: MutableList<ManualBrokerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTieredHoldVariancesFromQuota(items: MutableList<ManualBrokerQuota>): MutableList<TieredHoldVariance> {
    val out = mutableListOf<TieredHoldVariance>()
    for (item in items) {
        val derived = newTieredHoldVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignTieredHoldVariance(item: TieredHoldVariance): Double = manualBrokerQuotaWeight(1, true)
