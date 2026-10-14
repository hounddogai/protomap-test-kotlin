package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.broker.checkpoint.CappedBrokerMapping
import com.acme.transport.customs.broker.checkpoint.normalizeCappedBrokerCheckpointCode

fun summarizeCappedBrokerMappingsForSnapshot(items: MutableList<CappedBrokerMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePrimaryBrokerSnapshotsFromMapping(items: MutableList<CappedBrokerMapping>): MutableList<PrimaryBrokerSnapshot> {
    val out = mutableListOf<PrimaryBrokerSnapshot>()
    for (item in items) {
        val derived = newPrimaryBrokerSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyPrimaryBrokerSnapshot(item: PrimaryBrokerSnapshot): String {
    return normalizeCappedBrokerCheckpointCode(item.code)
}
