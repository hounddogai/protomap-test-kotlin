package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.fuelsurcharge.summary.TieredFuelSurchargeSummary
import com.acme.transport.freightpay.fuelsurcharge.summary.classifyWeightedFuelSurchargeBaseline

fun summarizeTieredFuelSurchargeSummariesForVariance(items: MutableList<TieredFuelSurchargeSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredFreightInvoiceVariancesFromSummary(items: MutableList<TieredFuelSurchargeSummary>): MutableList<TieredFreightInvoiceVariance> {
    val out = mutableListOf<TieredFreightInvoiceVariance>()
    for (item in items) {
        val derived = newTieredFreightInvoiceVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignTieredFreightInvoiceVariance(item: TieredFreightInvoiceVariance): String {
    return classifyWeightedFuelSurchargeBaseline(item.effectiveExposure())
}
