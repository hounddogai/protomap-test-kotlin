package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.audit.auditrollup.PendingAuditBatch
import com.acme.transport.freightpay.audit.auditrollup.normalizePendingAuditBatchCode

fun summarizePendingAuditBatchesForSummary(items: MutableList<PendingAuditBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAuditSummariesFromBatch(items: MutableList<PendingAuditBatch>): MutableList<AuditSummary> {
    val out = mutableListOf<AuditSummary>()
    for (item in items) {
        val derived = newAuditSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchAuditSummary(item: AuditSummary): String = normalizePendingAuditBatchCode(item.code)
