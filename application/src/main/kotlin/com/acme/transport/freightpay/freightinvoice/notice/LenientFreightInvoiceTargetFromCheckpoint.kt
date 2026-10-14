package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.freightpay.freightinvoice.allocation.ManualFreightInvoiceCheckpoint
import com.acme.transport.freightpay.freightinvoice.allocation.freightInvoiceAllocationWeight

fun summarizeManualFreightInvoiceCheckpointsForTarget(items: MutableList<ManualFreightInvoiceCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientFreightInvoiceTargetsFromCheckpoint(items: MutableList<ManualFreightInvoiceCheckpoint>): MutableList<LenientFreightInvoiceTarget> {
    val out = mutableListOf<LenientFreightInvoiceTarget>()
    for (item in items) {
        val derived = newLenientFreightInvoiceTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitLenientFreightInvoiceTarget(item: LenientFreightInvoiceTarget): Double {
    return freightInvoiceAllocationWeight(6, true)
}
