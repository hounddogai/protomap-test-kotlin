package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.freightpay.audit.recorditem.PendingAuditRecord
import com.acme.transport.freightpay.audit.recorditem.normalizeLenientAuditEstimateCode

fun summarizePendingAuditRecordsForEntry(items: MutableList<PendingAuditRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultRateCardEntriesFromRecord(items: MutableList<PendingAuditRecord>): MutableList<DefaultRateCardEntry> {
    val out = mutableListOf<DefaultRateCardEntry>()
    for (item in items) {
        val derived = newDefaultRateCardEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultRateCardEntry(item: DefaultRateCardEntry): String = normalizeLenientAuditEstimateCode(item.code)
