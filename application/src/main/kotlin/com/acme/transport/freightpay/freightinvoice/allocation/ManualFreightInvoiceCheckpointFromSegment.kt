package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.freightpay.ratecard.segment.RollingRateCardSegment
import com.acme.transport.freightpay.ratecard.segment.classifyRollingRateCardSegment

fun summarizeRollingRateCardSegmentsForCheckpoint(items: MutableList<RollingRateCardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveManualFreightInvoiceCheckpointsFromSegment(items: MutableList<RollingRateCardSegment>): MutableList<ManualFreightInvoiceCheckpoint> {
    val out = mutableListOf<ManualFreightInvoiceCheckpoint>()
    for (item in items) {
        val derived = newManualFreightInvoiceCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyManualFreightInvoiceCheckpointExtra(item: ManualFreightInvoiceCheckpoint): String {
    return classifyRollingRateCardSegment(item.measuredValue())
}
