package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.freightinvoice.window.LenientFreightInvoiceDraft
import com.acme.transport.freightpay.freightinvoice.window.lenientFreightInvoiceDraftWeight

fun summarizeLenientFreightInvoiceDraftsForClaim(items: MutableList<LenientFreightInvoiceDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedAuditClaimsFromDraft(items: MutableList<LenientFreightInvoiceDraft>): MutableList<WeightedAuditClaim> {
    val out = mutableListOf<WeightedAuditClaim>()
    for (item in items) {
        val derived = newWeightedAuditClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignWeightedAuditClaim(item: WeightedAuditClaim): Double = lenientFreightInvoiceDraftWeight(3, true)
