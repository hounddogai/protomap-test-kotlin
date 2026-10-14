package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.freightpay.freightinvoice.group.FreightInvoiceGroup
import com.acme.transport.freightpay.freightinvoice.group.normalizeFreightInvoiceDigestCode

fun summarizeFreightInvoiceGroupsForEstimate(items: MutableList<FreightInvoiceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDisputeEstimatesFromGroup(items: MutableList<FreightInvoiceGroup>): MutableList<DisputeEstimate> {
    val out = mutableListOf<DisputeEstimate>()
    for (item in items) {
        val derived = newDisputeEstimate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDisputeEstimate(item: DisputeEstimate): String = normalizeFreightInvoiceDigestCode(item.code)
