package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.tariff.tariffprofile.LenientTariffEntry
import com.acme.transport.customs.tariff.tariffprofile.tieredTariffProfileBucket

fun summarizeLenientTariffEntriesForAudit(items: MutableList<LenientTariffEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalBrokerAuditsFromEntry(items: MutableList<LenientTariffEntry>): MutableList<RegionalBrokerAudit> {
    val out = mutableListOf<RegionalBrokerAudit>()
    for (item in items) {
        val derived = newRegionalBrokerAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRegionalBrokerAudit(item: RegionalBrokerAudit): String {
    return tieredTariffProfileBucket(item.weightedExposure())
}
