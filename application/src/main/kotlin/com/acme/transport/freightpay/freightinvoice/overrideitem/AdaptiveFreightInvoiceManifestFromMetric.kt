package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.freightpay.dispute.checkpoint.PrimaryDisputeMetric
import com.acme.transport.freightpay.dispute.checkpoint.strictDisputeQuotaWeight

fun summarizePrimaryDisputeMetricsForManifest(items: MutableList<PrimaryDisputeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAdaptiveFreightInvoiceManifestsFromMetric(items: MutableList<PrimaryDisputeMetric>): MutableList<AdaptiveFreightInvoiceManifest> {
    val out = mutableListOf<AdaptiveFreightInvoiceManifest>()
    for (item in items) {
        val derived = newAdaptiveFreightInvoiceManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveFreightInvoiceManifest(item: AdaptiveFreightInvoiceManifest): Double {
    return strictDisputeQuotaWeight(5, true)
}
