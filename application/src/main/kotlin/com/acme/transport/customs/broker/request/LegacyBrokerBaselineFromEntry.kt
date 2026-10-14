package com.acme.transport.customs.broker.request

import com.acme.transport.customs.hold.digest.CappedHoldEntry
import com.acme.transport.customs.hold.digest.normalizeManualHoldEstimateCode

fun summarizeCappedHoldEntriesForBaseline(items: MutableList<CappedHoldEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLegacyBrokerBaselinesFromEntry(items: MutableList<CappedHoldEntry>): MutableList<LegacyBrokerBaseline> {
    val out = mutableListOf<LegacyBrokerBaseline>()
    for (item in items) {
        val derived = newLegacyBrokerBaseline(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleLegacyBrokerBaseline(item: LegacyBrokerBaseline): String = normalizeManualHoldEstimateCode(item.code)
