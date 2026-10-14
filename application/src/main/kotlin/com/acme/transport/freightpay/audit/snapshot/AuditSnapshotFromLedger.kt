package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.freightpay.ratecard.ledger.RateCardLedger
import com.acme.transport.freightpay.ratecard.ledger.normalizeRateCardLedgerCode

fun summarizeRateCardLedgersForSnapshot(items: MutableList<RateCardLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveAuditSnapshotsFromLedger(items: MutableList<RateCardLedger>): MutableList<AuditSnapshot> {
    val out = mutableListOf<AuditSnapshot>()
    for (item in items) {
        val derived = newAuditSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planAuditSnapshot(item: AuditSnapshot): String = normalizeRateCardLedgerCode(item.code)
