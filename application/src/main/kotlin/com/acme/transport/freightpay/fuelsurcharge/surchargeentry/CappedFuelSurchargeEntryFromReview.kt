package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.DefaultFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.normalizeSeasonalFreightInvoiceRuleCode

fun summarizeDefaultFreightInvoiceReviewsForEntry(items: MutableList<DefaultFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCappedFuelSurchargeEntriesFromReview(items: MutableList<DefaultFreightInvoiceReview>): MutableList<CappedFuelSurchargeEntry> {
    val out = mutableListOf<CappedFuelSurchargeEntry>()
    for (item in items) {
        val derived = newCappedFuelSurchargeEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileCappedFuelSurchargeEntry(item: CappedFuelSurchargeEntry): String {
    return normalizeSeasonalFreightInvoiceRuleCode(item.code)
}
