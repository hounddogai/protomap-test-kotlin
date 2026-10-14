package com.acme.transport.customs.broker.agreement

import com.acme.transport.customs.broker.slot.CappedBrokerSlot
import com.acme.transport.customs.broker.slot.pendingBrokerAllocationBucket

fun summarizeCappedBrokerSlotsForAgreement(items: MutableList<CappedBrokerSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBrokerAgreementsFromSlot(items: MutableList<CappedBrokerSlot>): MutableList<BrokerAgreement> {
    val out = mutableListOf<BrokerAgreement>()
    for (item in items) {
        val derived = newBrokerAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBrokerAgreement(item: BrokerAgreement): String {
    return pendingBrokerAllocationBucket(item.effectiveExposure())
}
