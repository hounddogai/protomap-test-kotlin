package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.freightpay.ratecard.summary.LegacyRateCardSummary
import com.acme.transport.freightpay.ratecard.summary.normalizeLegacyRateCardSummaryCode

fun summarizeLegacyRateCardSummariesForCheckpoint(items: MutableList<LegacyRateCardSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackFreightInvoiceCheckpointsFromSummary(items: MutableList<LegacyRateCardSummary>): MutableList<FallbackFreightInvoiceCheckpoint> {
    val out = mutableListOf<FallbackFreightInvoiceCheckpoint>()
    for (item in items) {
        val derived = newFallbackFreightInvoiceCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFallbackFreightInvoiceCheckpoint(item: FallbackFreightInvoiceCheckpoint): String {
    return normalizeLegacyRateCardSummaryCode(item.code)
}
