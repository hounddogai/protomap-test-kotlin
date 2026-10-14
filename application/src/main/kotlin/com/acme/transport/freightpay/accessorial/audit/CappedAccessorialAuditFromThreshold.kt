package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.freightpay.freightinvoice.threshold.RegionalFreightInvoiceThreshold
import com.acme.transport.freightpay.freightinvoice.threshold.classifyRegionalFreightInvoiceThreshold

fun summarizeRegionalFreightInvoiceThresholdsForAudit(items: MutableList<RegionalFreightInvoiceThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedAccessorialAuditsFromThreshold(items: MutableList<RegionalFreightInvoiceThreshold>): MutableList<CappedAccessorialAudit> {
    val out = mutableListOf<CappedAccessorialAudit>()
    for (item in items) {
        val derived = newCappedAccessorialAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateCappedAccessorialAudit(item: CappedAccessorialAudit): String {
    return classifyRegionalFreightInvoiceThreshold(item.adjustedVolume())
}
