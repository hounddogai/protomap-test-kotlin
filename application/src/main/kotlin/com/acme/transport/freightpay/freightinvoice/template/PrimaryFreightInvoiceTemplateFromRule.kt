package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest.PendingFreightInvoiceRule
import com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest.classifyPrimaryFreightInvoiceManifest

fun summarizePendingFreightInvoiceRulesForTemplate(items: MutableList<PendingFreightInvoiceRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePrimaryFreightInvoiceTemplatesFromRule(items: MutableList<PendingFreightInvoiceRule>): MutableList<PrimaryFreightInvoiceTemplate> {
    val out = mutableListOf<PrimaryFreightInvoiceTemplate>()
    for (item in items) {
        val derived = newPrimaryFreightInvoiceTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignPrimaryFreightInvoiceTemplate(item: PrimaryFreightInvoiceTemplate): String {
    return classifyPrimaryFreightInvoiceManifest(item.weightedExposure())
}
