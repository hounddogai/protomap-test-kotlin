package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.customs.broker.snapshot.RollingBrokerQuota
import com.acme.transport.customs.broker.snapshot.normalizeBrokerSnapshotCode

fun summarizeRollingBrokerQuotasForProfile(items: MutableList<RollingBrokerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveTieredTariffProfilesFromQuota(items: MutableList<RollingBrokerQuota>): MutableList<TieredTariffProfile> {
    val out = mutableListOf<TieredTariffProfile>()
    for (item in items) {
        val derived = newTieredTariffProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileTieredTariffProfile(item: TieredTariffProfile): String = normalizeBrokerSnapshotCode(item.code)
