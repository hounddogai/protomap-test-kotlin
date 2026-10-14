package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.ratecard.entry.DefaultRateCardEntry
import com.acme.transport.freightpay.ratecard.entry.classifyDefaultRateCardEntry

fun summarizeDefaultRateCardEntriesForRule(items: MutableList<DefaultRateCardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLegacyFreightInvoiceRulesFromEntry(items: MutableList<DefaultRateCardEntry>): MutableList<LegacyFreightInvoiceRule> {
    val out = mutableListOf<LegacyFreightInvoiceRule>()
    for (item in items) {
        val derived = newLegacyFreightInvoiceRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyFreightInvoiceRule(item: LegacyFreightInvoiceRule): String {
    return classifyDefaultRateCardEntry(item.netTotal())
}
