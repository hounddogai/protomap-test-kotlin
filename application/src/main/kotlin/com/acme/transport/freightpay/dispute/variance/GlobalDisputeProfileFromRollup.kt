package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.freightinvoice.invoicerollup.GlobalFreightInvoiceRollup

fun summarizeGlobalFreightInvoiceRollupsForProfile(items: MutableList<GlobalFreightInvoiceRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveGlobalDisputeProfilesFromRollup(items: MutableList<GlobalFreightInvoiceRollup>): MutableList<GlobalDisputeProfile> {
    val out = mutableListOf<GlobalDisputeProfile>()
    for (item in items) {
        val derived = newGlobalDisputeProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}
