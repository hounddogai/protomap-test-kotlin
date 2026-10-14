package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.freightinvoice.invoicemanifest.FreightInvoicePlan
import com.acme.transport.freightpay.freightinvoice.invoicemanifest.strictFreightInvoiceOverrideBucket

fun summarizeFreightInvoicePlansForQuota(items: MutableList<FreightInvoicePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDisputeQuotasFromPlan(items: MutableList<FreightInvoicePlan>): MutableList<DisputeQuota> {
    val out = mutableListOf<DisputeQuota>()
    for (item in items) {
        val derived = newDisputeQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDisputeQuota(item: DisputeQuota): String = strictFreightInvoiceOverrideBucket(item.netTotal())
