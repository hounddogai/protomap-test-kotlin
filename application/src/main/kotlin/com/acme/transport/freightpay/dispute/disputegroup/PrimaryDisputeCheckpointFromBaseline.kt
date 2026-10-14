package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.freightpay.freightinvoice.baseline.RollingFreightInvoiceBaseline
import com.acme.transport.freightpay.freightinvoice.baseline.rollingFreightInvoiceBaselineBucket

fun summarizeRollingFreightInvoiceBaselinesForCheckpoint(items: MutableList<RollingFreightInvoiceBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun derivePrimaryDisputeCheckpointsFromBaseline(items: MutableList<RollingFreightInvoiceBaseline>): MutableList<PrimaryDisputeCheckpoint> {
    val out = mutableListOf<PrimaryDisputeCheckpoint>()
    for (item in items) {
        val derived = newPrimaryDisputeCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignPrimaryDisputeCheckpoint(item: PrimaryDisputeCheckpoint): String {
    return rollingFreightInvoiceBaselineBucket(item.adjustedVolume())
}
