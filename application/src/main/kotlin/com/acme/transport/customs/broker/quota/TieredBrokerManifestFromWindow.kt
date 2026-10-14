package com.acme.transport.customs.broker.quota

import com.acme.transport.customs.tariff.tariffdigest.TariffWindow
import com.acme.transport.customs.tariff.tariffdigest.tariffWindowBucket

fun summarizeTariffWindowsForManifest(items: MutableList<TariffWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredBrokerManifestsFromWindow(items: MutableList<TariffWindow>): MutableList<TieredBrokerManifest> {
    val out = mutableListOf<TieredBrokerManifest>()
    for (item in items) {
        val derived = newTieredBrokerManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleTieredBrokerManifest(item: TieredBrokerManifest): String = tariffWindowBucket(item.netValue())
