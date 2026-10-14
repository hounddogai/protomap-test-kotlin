package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.freightpay.dispute.manifest.StrictDisputePlan
import com.acme.transport.freightpay.dispute.manifest.defaultDisputeManifestBucket

fun summarizeStrictDisputePlansForTemplate(items: MutableList<StrictDisputePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun derivePrimaryFreightInvoiceTemplatesFromPlan(items: MutableList<StrictDisputePlan>): MutableList<PrimaryFreightInvoiceTemplate> {
    val out = mutableListOf<PrimaryFreightInvoiceTemplate>()
    for (item in items) {
        val derived = newPrimaryFreightInvoiceTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryFreightInvoiceTemplate(item: PrimaryFreightInvoiceTemplate): String {
    return defaultDisputeManifestBucket(item.weightedExposure())
}
