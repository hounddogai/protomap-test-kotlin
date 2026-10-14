package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.linehaul.relay.mapping.RelayRecord
import com.acme.transport.linehaul.relay.mapping.normalizeRelayEntryCode

fun summarizeRelayRecordsForAgreement(items: MutableList<RelayRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCapacityAgreementsFromRecord(items: MutableList<RelayRecord>): MutableList<CapacityAgreement> {
    val out = mutableListOf<CapacityAgreement>()
    for (item in items) {
        val derived = newCapacityAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyCapacityAgreement(item: CapacityAgreement): String = normalizeRelayEntryCode(item.code)
