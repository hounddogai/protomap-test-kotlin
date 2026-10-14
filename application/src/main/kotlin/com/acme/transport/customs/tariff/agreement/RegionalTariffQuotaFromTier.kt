package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.broker.baseline.BrokerTier
import com.acme.transport.customs.broker.baseline.cappedBrokerRecordWeight

fun summarizeBrokerTiersForQuota(items: MutableList<BrokerTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRegionalTariffQuotasFromTier(items: MutableList<BrokerTier>): MutableList<RegionalTariffQuota> {
    val out = mutableListOf<RegionalTariffQuota>()
    for (item in items) {
        val derived = newRegionalTariffQuota(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalTariffQuota(item: RegionalTariffQuota): Double = cappedBrokerRecordWeight(5, true)
