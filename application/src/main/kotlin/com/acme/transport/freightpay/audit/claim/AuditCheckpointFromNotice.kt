package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.audit.batch.DefaultAuditNotice
import com.acme.transport.freightpay.audit.batch.classifyManualAuditProposal

fun summarizeDefaultAuditNoticesForCheckpoint(items: MutableList<DefaultAuditNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveAuditCheckpointsFromNotice(items: MutableList<DefaultAuditNotice>): MutableList<AuditCheckpoint> {
    val out = mutableListOf<AuditCheckpoint>()
    for (item in items) {
        val derived = newAuditCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignAuditCheckpoint(item: AuditCheckpoint): String = classifyManualAuditProposal(item.adjustedValue())
