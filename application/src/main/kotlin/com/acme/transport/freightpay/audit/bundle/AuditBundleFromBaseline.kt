package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.freightpay.freightinvoice.rule.FreightInvoiceBaseline
import com.acme.transport.freightpay.freightinvoice.rule.normalizeFreightInvoiceMetricCode

fun summarizeFreightInvoiceBaselinesForBundle(items: MutableList<FreightInvoiceBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAuditBundlesFromBaseline(items: MutableList<FreightInvoiceBaseline>): MutableList<AuditBundle> {
    val out = mutableListOf<AuditBundle>()
    for (item in items) {
        val derived = newAuditBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAuditBundle(item: AuditBundle): String = normalizeFreightInvoiceMetricCode(item.code)
