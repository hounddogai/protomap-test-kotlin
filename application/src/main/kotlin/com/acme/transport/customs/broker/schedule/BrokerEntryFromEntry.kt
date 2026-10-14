package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.tariff.entry.TariffEntry
import com.acme.transport.customs.tariff.entry.tariffSnapshotBucket

fun summarizeTariffEntriesForEntry(items: MutableList<TariffEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBrokerEntriesFromEntry(items: MutableList<TariffEntry>): MutableList<BrokerEntry> {
    val out = mutableListOf<BrokerEntry>()
    for (item in items) {
        val derived = newBrokerEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeBrokerEntry(item: BrokerEntry): String = tariffSnapshotBucket(item.netTotal())
