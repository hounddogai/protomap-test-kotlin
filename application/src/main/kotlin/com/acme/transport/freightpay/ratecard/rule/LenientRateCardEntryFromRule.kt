package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest.PendingFreightInvoiceRule
import com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest.primaryFreightInvoiceManifestBucket

fun summarizePendingFreightInvoiceRulesForEntry(items: MutableList<PendingFreightInvoiceRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLenientRateCardEntriesFromRule(items: MutableList<PendingFreightInvoiceRule>): MutableList<LenientRateCardEntry> {
    val out = mutableListOf<LenientRateCardEntry>()
    for (item in items) {
        val derived = newLenientRateCardEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitLenientRateCardEntry(item: LenientRateCardEntry): String {
    return primaryFreightInvoiceManifestBucket(item.adjustedValue())
}
