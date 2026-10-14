package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.freightinvoice.invoiceentry.BatchedFreightInvoiceAllocation
import com.acme.transport.freightpay.freightinvoice.invoiceentry.weightedFreightInvoiceProposalBucket

fun summarizeBatchedFreightInvoiceAllocationsForForecast(items: MutableList<BatchedFreightInvoiceAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveFreightInvoiceForecastsFromAllocation(items: MutableList<BatchedFreightInvoiceAllocation>): MutableList<FreightInvoiceForecast> {
    val out = mutableListOf<FreightInvoiceForecast>()
    for (item in items) {
        val derived = newFreightInvoiceForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankFreightInvoiceForecast(item: FreightInvoiceForecast): String {
    return weightedFreightInvoiceProposalBucket(item.weightedVolume())
}
