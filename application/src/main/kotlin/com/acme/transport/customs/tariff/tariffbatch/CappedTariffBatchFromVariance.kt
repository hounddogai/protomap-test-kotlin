package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.customs.broker.bundle.CappedBrokerVariance
import com.acme.transport.customs.broker.bundle.brokerBundleBucket

fun summarizeCappedBrokerVariancesForBatch(items: MutableList<CappedBrokerVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedTariffBatchesFromVariance(items: MutableList<CappedBrokerVariance>): MutableList<CappedTariffBatch> {
    val out = mutableListOf<CappedTariffBatch>()
    for (item in items) {
        val derived = newCappedTariffBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitCappedTariffBatch(item: CappedTariffBatch): String = brokerBundleBucket(item.weightedTotal())
