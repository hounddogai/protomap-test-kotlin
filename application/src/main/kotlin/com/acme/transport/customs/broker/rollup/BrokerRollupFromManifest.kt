package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.broker.schedule.CappedBrokerManifest
import com.acme.transport.customs.broker.schedule.normalizeBrokerEntryCode

fun summarizeCappedBrokerManifestsForRollup(items: MutableList<CappedBrokerManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBrokerRollupsFromManifest(items: MutableList<CappedBrokerManifest>): MutableList<BrokerRollup> {
    val out = mutableListOf<BrokerRollup>()
    for (item in items) {
        val derived = newBrokerRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBrokerRollup(item: BrokerRollup): String = normalizeBrokerEntryCode(item.code)
