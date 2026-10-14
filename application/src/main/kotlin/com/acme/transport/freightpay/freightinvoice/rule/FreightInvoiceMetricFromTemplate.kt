package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.audit.template.AuditTemplate
import com.acme.transport.freightpay.audit.template.classifyAuditSlot

fun summarizeAuditTemplatesForMetric(items: MutableList<AuditTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFreightInvoiceMetricsFromTemplate(items: MutableList<AuditTemplate>): MutableList<FreightInvoiceMetric> {
    val out = mutableListOf<FreightInvoiceMetric>()
    for (item in items) {
        val derived = newFreightInvoiceMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyFreightInvoiceMetric(item: FreightInvoiceMetric): String = classifyAuditSlot(item.weightedVolume())
