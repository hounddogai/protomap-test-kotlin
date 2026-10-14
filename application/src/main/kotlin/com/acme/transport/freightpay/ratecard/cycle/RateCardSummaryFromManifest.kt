package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.freightpay.freightinvoice.manifest.FreightInvoiceManifest
import com.acme.transport.freightpay.freightinvoice.manifest.normalizeFreightInvoiceReviewCode

fun summarizeFreightInvoiceManifestsForSummary(items: MutableList<FreightInvoiceManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRateCardSummariesFromManifest(items: MutableList<FreightInvoiceManifest>): MutableList<RateCardSummary> {
    val out = mutableListOf<RateCardSummary>()
    for (item in items) {
        val derived = newRateCardSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRateCardSummary(item: RateCardSummary): String = normalizeFreightInvoiceReviewCode(item.code)
