package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.tariff.tariffagreement.StrictTariffScore
import com.acme.transport.customs.tariff.tariffagreement.pendingTariffAgreementBucket

fun summarizeStrictTariffScoresForScore(items: MutableList<StrictTariffScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedBrokerScoresFromScore(items: MutableList<StrictTariffScore>): MutableList<WeightedBrokerScore> {
    val out = mutableListOf<WeightedBrokerScore>()
    for (item in items) {
        val derived = newWeightedBrokerScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyWeightedBrokerScore(item: WeightedBrokerScore): String {
    return pendingTariffAgreementBucket(item.effectiveVolume())
}
