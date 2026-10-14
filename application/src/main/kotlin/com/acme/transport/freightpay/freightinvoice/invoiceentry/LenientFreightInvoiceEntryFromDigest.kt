package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.freightpay.audit.legacyauditpolicy.CappedAuditDigest
import com.acme.transport.freightpay.audit.legacyauditpolicy.primaryAuditPolicyBucket

fun summarizeCappedAuditDigestsForEntry(items: MutableList<CappedAuditDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLenientFreightInvoiceEntriesFromDigest(items: MutableList<CappedAuditDigest>): MutableList<LenientFreightInvoiceEntry> {
    val out = mutableListOf<LenientFreightInvoiceEntry>()
    for (item in items) {
        val derived = newLenientFreightInvoiceEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientFreightInvoiceEntry(item: LenientFreightInvoiceEntry): String {
    return primaryAuditPolicyBucket(item.measuredValue())
}
