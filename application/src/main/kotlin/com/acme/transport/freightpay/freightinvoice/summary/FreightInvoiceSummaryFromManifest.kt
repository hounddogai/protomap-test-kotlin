package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.freightinvoice.overrideitem.AdaptiveFreightInvoiceManifest
import com.acme.transport.freightpay.freightinvoice.overrideitem.globalFreightInvoiceRuleBucket

fun summarizeAdaptiveFreightInvoiceManifestsForSummary(items: MutableList<AdaptiveFreightInvoiceManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFreightInvoiceSummariesFromManifest(items: MutableList<AdaptiveFreightInvoiceManifest>): MutableList<FreightInvoiceSummary> {
    val out = mutableListOf<FreightInvoiceSummary>()
    for (item in items) {
        val derived = newFreightInvoiceSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFreightInvoiceSummary(item: FreightInvoiceSummary): String {
    return globalFreightInvoiceRuleBucket(item.weightedTotal())
}
