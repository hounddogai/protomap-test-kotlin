package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.freightinvoice.manifest.FreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.manifest.normalizeFreightInvoiceReviewCode

fun summarizeFreightInvoiceReviewsForAdjustment(items: MutableList<FreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFuelSurchargeAdjustmentsFromReview(items: MutableList<FreightInvoiceReview>): MutableList<FuelSurchargeAdjustment> {
    val out = mutableListOf<FuelSurchargeAdjustment>()
    for (item in items) {
        val derived = newFuelSurchargeAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeFuelSurchargeAdjustment(item: FuelSurchargeAdjustment): String {
    return normalizeFreightInvoiceReviewCode(item.code)
}
