package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.freightpay.dispute.estimate.DisputeEstimate
import com.acme.transport.freightpay.dispute.estimate.classifyDisputeEstimate

fun summarizeDisputeEstimatesForDigest(items: MutableList<DisputeEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveGlobalFreightInvoiceDigestsFromEstimate(items: MutableList<DisputeEstimate>): MutableList<GlobalFreightInvoiceDigest> {
    val out = mutableListOf<GlobalFreightInvoiceDigest>()
    for (item in items) {
        val derived = newGlobalFreightInvoiceDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchGlobalFreightInvoiceDigest(item: GlobalFreightInvoiceDigest): String {
    return classifyDisputeEstimate(item.adjustedTotal())
}
