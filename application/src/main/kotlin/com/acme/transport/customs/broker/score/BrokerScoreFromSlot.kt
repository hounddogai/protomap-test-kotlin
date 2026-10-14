package com.acme.transport.customs.broker.score

import com.acme.transport.customs.tariff.slot.TariffSlot
import com.acme.transport.customs.tariff.slot.tariffSlotBucket

fun summarizeTariffSlotsForScore(items: MutableList<TariffSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBrokerScoresFromSlot(items: MutableList<TariffSlot>): MutableList<BrokerScore> {
    val out = mutableListOf<BrokerScore>()
    for (item in items) {
        val derived = newBrokerScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectBrokerScore(item: BrokerScore): String = tariffSlotBucket(item.netValue())
