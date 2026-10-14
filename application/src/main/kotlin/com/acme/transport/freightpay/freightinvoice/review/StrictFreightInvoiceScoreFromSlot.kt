package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.accessorial.accessorialadjustment.RegionalAccessorialSlot
import com.acme.transport.freightpay.accessorial.accessorialadjustment.classifyPendingAccessorialOverride

fun summarizeRegionalAccessorialSlotsForScore(items: MutableList<RegionalAccessorialSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictFreightInvoiceScoresFromSlot(items: MutableList<RegionalAccessorialSlot>): MutableList<StrictFreightInvoiceScore> {
    val out = mutableListOf<StrictFreightInvoiceScore>()
    for (item in items) {
        val derived = newStrictFreightInvoiceScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictFreightInvoiceScore(item: StrictFreightInvoiceScore): String {
    return classifyPendingAccessorialOverride(item.measuredValue())
}
