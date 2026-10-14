package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.freightinvoice.target.GlobalFreightInvoiceDigest
import com.acme.transport.freightpay.freightinvoice.target.normalizeGlobalFreightInvoiceDigestCode

fun summarizeGlobalFreightInvoiceDigestsForBaseline(items: MutableList<GlobalFreightInvoiceDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDisputeBaselinesFromDigest(items: MutableList<GlobalFreightInvoiceDigest>): MutableList<DisputeBaseline> {
    val out = mutableListOf<DisputeBaseline>()
    for (item in items) {
        val derived = newDisputeBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDisputeBaseline(item: DisputeBaseline): String = normalizeGlobalFreightInvoiceDigestCode(item.code)
