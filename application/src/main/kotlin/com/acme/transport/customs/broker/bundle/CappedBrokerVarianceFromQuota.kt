package com.acme.transport.customs.broker.bundle

import com.acme.transport.customs.broker.quota.DefaultBrokerQuota
import com.acme.transport.customs.broker.quota.defaultBrokerQuotaWeight

fun summarizeDefaultBrokerQuotasForVariance(items: MutableList<DefaultBrokerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCappedBrokerVariancesFromQuota(items: MutableList<DefaultBrokerQuota>): MutableList<CappedBrokerVariance> {
    val out = mutableListOf<CappedBrokerVariance>()
    for (item in items) {
        val derived = newCappedBrokerVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveCappedBrokerVariance(item: CappedBrokerVariance): Double = defaultBrokerQuotaWeight(1, true)
