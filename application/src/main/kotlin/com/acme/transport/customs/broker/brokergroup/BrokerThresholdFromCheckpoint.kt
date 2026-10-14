package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.broker.profile.RegionalBrokerCheckpoint
import com.acme.transport.customs.broker.profile.classifyRegionalBrokerCheckpoint

fun summarizeRegionalBrokerCheckpointsForThreshold(items: MutableList<RegionalBrokerCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveBrokerThresholdsFromCheckpoint(items: MutableList<RegionalBrokerCheckpoint>): MutableList<BrokerThreshold> {
    val out = mutableListOf<BrokerThreshold>()
    for (item in items) {
        val derived = newBrokerThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankBrokerThreshold(item: BrokerThreshold): String = classifyRegionalBrokerCheckpoint(item.netVolume())
