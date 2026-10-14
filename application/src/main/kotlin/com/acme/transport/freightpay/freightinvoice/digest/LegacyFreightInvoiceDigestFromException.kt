package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.audit.exception.AuditException
import com.acme.transport.freightpay.audit.exception.adaptiveAuditManifestBucket

fun summarizeAuditExceptionsForDigest(items: MutableList<AuditException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLegacyFreightInvoiceDigestsFromException(items: MutableList<AuditException>): MutableList<LegacyFreightInvoiceDigest> {
    val out = mutableListOf<LegacyFreightInvoiceDigest>()
    for (item in items) {
        val derived = newLegacyFreightInvoiceDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLegacyFreightInvoiceDigest(item: LegacyFreightInvoiceDigest): String {
    return adaptiveAuditManifestBucket(item.effectiveVolume())
}
