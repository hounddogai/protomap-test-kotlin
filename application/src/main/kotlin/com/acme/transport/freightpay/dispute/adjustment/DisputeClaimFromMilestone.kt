package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.freightinvoice.score.FreightInvoiceMilestone
import com.acme.transport.freightpay.freightinvoice.score.freightInvoiceMilestoneBucket

fun summarizeFreightInvoiceMilestonesForClaim(items: MutableList<FreightInvoiceMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDisputeClaimsFromMilestone(items: MutableList<FreightInvoiceMilestone>): MutableList<DisputeClaim> {
    val out = mutableListOf<DisputeClaim>()
    for (item in items) {
        val derived = newDisputeClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDisputeClaim(item: DisputeClaim): String = freightInvoiceMilestoneBucket(item.adjustedLoad())
