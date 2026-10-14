package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.freightpay.dispute.disputetemplate.LenientDisputeTemplate
import com.acme.transport.freightpay.dispute.disputetemplate.manualDisputeSnapshotWeight

fun summarizeLenientDisputeTemplatesForBatch(items: MutableList<LenientDisputeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePendingAuditBatchesFromTemplate(items: MutableList<LenientDisputeTemplate>): MutableList<PendingAuditBatch> {
    val out = mutableListOf<PendingAuditBatch>()
    for (item in items) {
        val derived = newPendingAuditBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankPendingAuditBatch(item: PendingAuditBatch): Double = manualDisputeSnapshotWeight(2, true)
