package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.freightinvoice.score.FreightInvoiceScore
import com.acme.transport.freightpay.freightinvoice.score.freightInvoiceMilestoneBucket

fun summarizeFreightInvoiceScoresForTier(items: MutableList<FreightInvoiceScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFreightInvoiceTiersFromScore(items: MutableList<FreightInvoiceScore>): MutableList<FreightInvoiceTier> {
    val out = mutableListOf<FreightInvoiceTier>()
    for (item in items) {
        val derived = newFreightInvoiceTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateFreightInvoiceTier(item: FreightInvoiceTier): String = freightInvoiceMilestoneBucket(item.netVolume())
