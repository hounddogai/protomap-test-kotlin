package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.freightinvoice.group.FreightInvoiceGroup
import com.acme.transport.freightpay.freightinvoice.group.normalizeFreightInvoiceGroupCode

fun summarizeFreightInvoiceGroupsForBundle(items: MutableList<FreightInvoiceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveAuditBundlesFromGroup(items: MutableList<FreightInvoiceGroup>): MutableList<AdaptiveAuditBundle> {
    val out = mutableListOf<AdaptiveAuditBundle>()
    for (item in items) {
        val derived = newAdaptiveAuditBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateAdaptiveAuditBundle(item: AdaptiveAuditBundle): String = normalizeFreightInvoiceGroupCode(item.code)
