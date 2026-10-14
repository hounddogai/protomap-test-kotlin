package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.freightpay.audit.auditoverride.GlobalAuditBundle
import com.acme.transport.freightpay.audit.auditoverride.normalizeGlobalAuditBundleCode

fun summarizeGlobalAuditBundlesForTarget(items: MutableList<GlobalAuditBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientFreightInvoiceTargetsFromBundle(items: MutableList<GlobalAuditBundle>): MutableList<LenientFreightInvoiceTarget> {
    val out = mutableListOf<LenientFreightInvoiceTarget>()
    for (item in items) {
        val derived = newLenientFreightInvoiceTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientFreightInvoiceTarget(item: LenientFreightInvoiceTarget): String {
    return normalizeGlobalAuditBundleCode(item.code)
}
