package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.freightpay.freightinvoice.manifest.FreightInvoiceDraft
import com.acme.transport.freightpay.freightinvoice.manifest.normalizeFreightInvoiceCheckpointCode

fun summarizeFreightInvoiceDraftsForBatch(items: MutableList<FreightInvoiceDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAccessorialBatchesFromDraft(items: MutableList<FreightInvoiceDraft>): MutableList<AccessorialBatch> {
    val out = mutableListOf<AccessorialBatch>()
    for (item in items) {
        val derived = newAccessorialBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAccessorialBatchExtra(item: AccessorialBatch): String {
    return normalizeFreightInvoiceCheckpointCode(item.code)
}
