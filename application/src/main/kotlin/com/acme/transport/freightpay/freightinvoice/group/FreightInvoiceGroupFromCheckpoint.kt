package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.freightpay.freightinvoice.schedule.FallbackFreightInvoiceCheckpoint
import com.acme.transport.freightpay.freightinvoice.schedule.freightInvoiceScheduleBucket

fun summarizeFallbackFreightInvoiceCheckpointsForGroup(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFreightInvoiceGroupsFromCheckpoint(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableList<FreightInvoiceGroup> {
    val out = mutableListOf<FreightInvoiceGroup>()
    for (item in items) {
        val derived = newFreightInvoiceGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFreightInvoiceGroup(item: FreightInvoiceGroup): String {
    return freightInvoiceScheduleBucket(item.measuredValue())
}
