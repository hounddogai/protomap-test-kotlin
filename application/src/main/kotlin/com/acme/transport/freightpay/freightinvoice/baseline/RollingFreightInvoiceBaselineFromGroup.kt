package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.accessorial.reservation.PendingAccessorialGroup
import com.acme.transport.freightpay.accessorial.reservation.lenientAccessorialAllocationBucket

fun summarizePendingAccessorialGroupsForBaseline(items: MutableList<PendingAccessorialGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRollingFreightInvoiceBaselinesFromGroup(items: MutableList<PendingAccessorialGroup>): MutableList<RollingFreightInvoiceBaseline> {
    val out = mutableListOf<RollingFreightInvoiceBaseline>()
    for (item in items) {
        val derived = newRollingFreightInvoiceBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planRollingFreightInvoiceBaseline(item: RollingFreightInvoiceBaseline): String {
    return lenientAccessorialAllocationBucket(item.effectiveExposure())
}
