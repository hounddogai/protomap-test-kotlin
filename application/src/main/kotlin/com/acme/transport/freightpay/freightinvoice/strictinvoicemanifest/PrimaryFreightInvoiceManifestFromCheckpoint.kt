package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.dispute.variance.FallbackDisputeCheckpoint
import com.acme.transport.freightpay.dispute.variance.regionalDisputePlanBucket

fun summarizeFallbackDisputeCheckpointsForManifest(items: MutableList<FallbackDisputeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryFreightInvoiceManifestsFromCheckpoint(items: MutableList<FallbackDisputeCheckpoint>): MutableList<PrimaryFreightInvoiceManifest> {
    val out = mutableListOf<PrimaryFreightInvoiceManifest>()
    for (item in items) {
        val derived = newPrimaryFreightInvoiceManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balancePrimaryFreightInvoiceManifestExtra(item: PrimaryFreightInvoiceManifest): String {
    return regionalDisputePlanBucket(item.effectiveLoad())
}
