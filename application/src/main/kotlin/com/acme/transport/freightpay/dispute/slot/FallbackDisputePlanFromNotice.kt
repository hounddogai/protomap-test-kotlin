package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.freightinvoice.notice.PrimaryFreightInvoiceNotice
import com.acme.transport.freightpay.freightinvoice.notice.classifyFallbackFreightInvoiceAllocation

fun summarizePrimaryFreightInvoiceNoticesForPlan(items: MutableList<PrimaryFreightInvoiceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFallbackDisputePlansFromNotice(items: MutableList<PrimaryFreightInvoiceNotice>): MutableList<FallbackDisputePlan> {
    val out = mutableListOf<FallbackDisputePlan>()
    for (item in items) {
        val derived = newFallbackDisputePlan(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankFallbackDisputePlan(item: FallbackDisputePlan): String {
    return classifyFallbackFreightInvoiceAllocation(item.weightedExposure())
}
