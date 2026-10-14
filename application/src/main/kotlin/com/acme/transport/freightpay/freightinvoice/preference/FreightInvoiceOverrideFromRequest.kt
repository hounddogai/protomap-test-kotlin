package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.dispute.window.DisputeRequest
import com.acme.transport.freightpay.dispute.window.disputeExceptionWeight

fun summarizeDisputeRequestsForOverride(items: MutableList<DisputeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveFreightInvoiceOverridesFromRequest(items: MutableList<DisputeRequest>): MutableList<FreightInvoiceOverride> {
    val out = mutableListOf<FreightInvoiceOverride>()
    for (item in items) {
        val derived = newFreightInvoiceOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankFreightInvoiceOverride(item: FreightInvoiceOverride): Double = disputeExceptionWeight(9, true)
