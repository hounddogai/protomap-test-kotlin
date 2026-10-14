package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.tariff.profile.TariffBaseline
import com.acme.transport.customs.tariff.profile.tariffAllocationWeight

fun summarizeTariffBaselinesForEstimate(items: MutableList<TariffBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBrokerEstimatesFromBaseline(items: MutableList<TariffBaseline>): MutableList<BrokerEstimate> {
    val out = mutableListOf<BrokerEstimate>()
    for (item in items) {
        val derived = newBrokerEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBrokerEstimate(item: BrokerEstimate): Double = tariffAllocationWeight(4, true)
