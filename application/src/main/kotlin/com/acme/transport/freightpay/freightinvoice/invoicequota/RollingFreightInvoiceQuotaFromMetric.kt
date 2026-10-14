package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric.BatchedAccessorialMetric
import com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric.classifyScheduledAccessorialDraft

fun summarizeBatchedAccessorialMetricsForQuota(items: MutableList<BatchedAccessorialMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingFreightInvoiceQuotasFromMetric(items: MutableList<BatchedAccessorialMetric>): MutableList<RollingFreightInvoiceQuota> {
    val out = mutableListOf<RollingFreightInvoiceQuota>()
    for (item in items) {
        val derived = newRollingFreightInvoiceQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleRollingFreightInvoiceQuota(item: RollingFreightInvoiceQuota): String {
    return classifyScheduledAccessorialDraft(item.projectedValue())
}
