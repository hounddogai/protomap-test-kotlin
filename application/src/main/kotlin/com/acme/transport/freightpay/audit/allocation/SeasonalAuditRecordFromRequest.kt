package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.freightpay.dispute.disputeassignment.GlobalDisputeRequest
import com.acme.transport.freightpay.dispute.disputeassignment.batchedDisputeRuleWeight

fun summarizeGlobalDisputeRequestsForRecord(items: MutableList<GlobalDisputeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSeasonalAuditRecordsFromRequest(items: MutableList<GlobalDisputeRequest>): MutableList<SeasonalAuditRecord> {
    val out = mutableListOf<SeasonalAuditRecord>()
    for (item in items) {
        val derived = newSeasonalAuditRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectSeasonalAuditRecord(item: SeasonalAuditRecord): Double = batchedDisputeRuleWeight(6, true)
