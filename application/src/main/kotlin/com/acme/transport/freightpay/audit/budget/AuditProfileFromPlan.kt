package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.freightinvoice.invoicemanifest.FreightInvoicePlan
import com.acme.transport.freightpay.freightinvoice.invoicemanifest.normalizeFallbackFreightInvoiceManifestCode

fun summarizeFreightInvoicePlansForProfile(items: MutableList<FreightInvoicePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAuditProfilesFromPlan(items: MutableList<FreightInvoicePlan>): MutableList<AuditProfile> {
    val out = mutableListOf<AuditProfile>()
    for (item in items) {
        val derived = newAuditProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankAuditProfile(item: AuditProfile): String = normalizeFallbackFreightInvoiceManifestCode(item.code)
