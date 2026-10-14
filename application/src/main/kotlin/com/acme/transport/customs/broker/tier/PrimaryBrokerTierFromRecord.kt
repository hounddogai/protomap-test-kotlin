package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.hold.recorditem.HoldRecord
import com.acme.transport.customs.hold.recorditem.holdRecordWeight

fun summarizeHoldRecordsForTier(items: MutableList<HoldRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryBrokerTiersFromRecord(items: MutableList<HoldRecord>): MutableList<PrimaryBrokerTier> {
    val out = mutableListOf<PrimaryBrokerTier>()
    for (item in items) {
        val derived = newPrimaryBrokerTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePrimaryBrokerTier(item: PrimaryBrokerTier): Double = holdRecordWeight(3, true)
