package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.freightinvoice.profile.FreightInvoiceLedger
import com.acme.transport.freightpay.freightinvoice.profile.normalizeFreightInvoiceProfileCode

fun summarizeFreightInvoiceLedgersForEntry(items: MutableList<FreightInvoiceLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveGlobalRateCardEntriesFromLedger(items: MutableList<FreightInvoiceLedger>): MutableList<GlobalRateCardEntry> {
    val out = mutableListOf<GlobalRateCardEntry>()
    for (item in items) {
        val derived = newGlobalRateCardEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeGlobalRateCardEntry(item: GlobalRateCardEntry): String {
    return normalizeFreightInvoiceProfileCode(item.code)
}
