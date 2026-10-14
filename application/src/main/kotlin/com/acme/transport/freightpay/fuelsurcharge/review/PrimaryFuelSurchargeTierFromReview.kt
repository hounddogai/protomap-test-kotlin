package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.freightinvoice.invoicereview.ManualFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.invoicereview.lenientFreightInvoiceProfileBucket

fun summarizeManualFreightInvoiceReviewsForTier(items: MutableList<ManualFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryFuelSurchargeTiersFromReview(items: MutableList<ManualFreightInvoiceReview>): MutableList<PrimaryFuelSurchargeTier> {
    val out = mutableListOf<PrimaryFuelSurchargeTier>()
    for (item in items) {
        val derived = newPrimaryFuelSurchargeTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcilePrimaryFuelSurchargeTier(item: PrimaryFuelSurchargeTier): String {
    return lenientFreightInvoiceProfileBucket(item.effectiveTotal())
}
