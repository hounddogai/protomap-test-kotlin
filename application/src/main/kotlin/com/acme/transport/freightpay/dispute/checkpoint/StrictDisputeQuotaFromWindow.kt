package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.audit.manifest.RegionalAuditWindow
import com.acme.transport.freightpay.audit.manifest.regionalAuditWindowBucket

fun summarizeRegionalAuditWindowsForQuota(items: MutableList<RegionalAuditWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictDisputeQuotasFromWindow(items: MutableList<RegionalAuditWindow>): MutableList<StrictDisputeQuota> {
    val out = mutableListOf<StrictDisputeQuota>()
    for (item in items) {
        val derived = newStrictDisputeQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictDisputeQuotaExtra(item: StrictDisputeQuota): String {
    return regionalAuditWindowBucket(item.effectiveLoad())
}
