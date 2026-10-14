package com.acme.transport.freightpay.audit.draft

import com.acme.transport.freightpay.dispute.ledger.DisputeCycle
import com.acme.transport.freightpay.dispute.ledger.rollingDisputeLedgerBucket

fun summarizeDisputeCyclesForDraft(items: MutableList<DisputeCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveSeasonalAuditDraftsFromCycle(items: MutableList<DisputeCycle>): MutableList<SeasonalAuditDraft> {
    val out = mutableListOf<SeasonalAuditDraft>()
    for (item in items) {
        val derived = newSeasonalAuditDraft(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planSeasonalAuditDraft(item: SeasonalAuditDraft): String = rollingDisputeLedgerBucket(item.effectiveValue())
