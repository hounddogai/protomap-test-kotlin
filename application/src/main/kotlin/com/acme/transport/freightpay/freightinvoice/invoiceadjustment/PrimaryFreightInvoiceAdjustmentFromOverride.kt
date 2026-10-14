package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.freightpay.accessorial.overrideitem.PrimaryAccessorialOverride
import com.acme.transport.freightpay.accessorial.overrideitem.seasonalAccessorialProposalBucket

fun summarizePrimaryAccessorialOverridesForAdjustment(items: MutableList<PrimaryAccessorialOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryFreightInvoiceAdjustmentsFromOverride(items: MutableList<PrimaryAccessorialOverride>): MutableList<PrimaryFreightInvoiceAdjustment> {
    val out = mutableListOf<PrimaryFreightInvoiceAdjustment>()
    for (item in items) {
        val derived = newPrimaryFreightInvoiceAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankPrimaryFreightInvoiceAdjustment(item: PrimaryFreightInvoiceAdjustment): String {
    return seasonalAccessorialProposalBucket(item.effectiveVolume())
}
