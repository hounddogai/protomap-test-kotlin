package com.acme.transport.freightpay.audit.batch

import com.acme.transport.freightpay.freightinvoice.summary.ScheduledFreightInvoiceSnapshot
import com.acme.transport.freightpay.freightinvoice.summary.scheduledFreightInvoiceSnapshotBucket

fun summarizeScheduledFreightInvoiceSnapshotsForRule(items: MutableList<ScheduledFreightInvoiceSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAuditRulesFromSnapshot(items: MutableList<ScheduledFreightInvoiceSnapshot>): MutableList<AuditRule> {
    val out = mutableListOf<AuditRule>()
    for (item in items) {
        val derived = newAuditRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignAuditRule(item: AuditRule): String = scheduledFreightInvoiceSnapshotBucket(item.projectedTotal())
