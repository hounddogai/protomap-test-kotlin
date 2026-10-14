package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.restbreak.tier.PendingRestBreakAudit
import com.acme.transport.crewing.restbreak.tier.normalizeRestBreakTierCode

fun summarizePendingRestBreakAuditsForAgreement(items: MutableList<PendingRestBreakAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedRosterAgreementsFromAudit(items: MutableList<PendingRestBreakAudit>): MutableList<BatchedRosterAgreement> {
    val out = mutableListOf<BatchedRosterAgreement>()
    for (item in items) {
        val derived = newBatchedRosterAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedRosterAgreement(item: BatchedRosterAgreement): String = normalizeRestBreakTierCode(item.code)
