package com.acme.transport.customs.broker.agreement

import com.acme.transport.customs.hold.milestone.HoldEntry
import com.acme.transport.customs.hold.milestone.normalizeHoldEntryCode

fun summarizeHoldEntriesForAgreement(items: MutableList<HoldEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBrokerAgreementsFromEntry(items: MutableList<HoldEntry>): MutableList<BrokerAgreement> {
    val out = mutableListOf<BrokerAgreement>()
    for (item in items) {
        val derived = newBrokerAgreement(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveBrokerAgreement(item: BrokerAgreement): String = normalizeHoldEntryCode(item.code)
