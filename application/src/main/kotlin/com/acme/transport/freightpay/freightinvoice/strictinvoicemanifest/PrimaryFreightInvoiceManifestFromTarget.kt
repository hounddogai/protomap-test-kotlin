package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.audit.auditclaim.CappedAuditTarget
import com.acme.transport.freightpay.audit.auditclaim.classifyRollingAuditClaim

fun summarizeCappedAuditTargetsForManifest(items: MutableList<CappedAuditTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryFreightInvoiceManifestsFromTarget(items: MutableList<CappedAuditTarget>): MutableList<PrimaryFreightInvoiceManifest> {
    val out = mutableListOf<PrimaryFreightInvoiceManifest>()
    for (item in items) {
        val derived = newPrimaryFreightInvoiceManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balancePrimaryFreightInvoiceManifest(item: PrimaryFreightInvoiceManifest): String {
    return classifyRollingAuditClaim(item.effectiveLoad())
}
