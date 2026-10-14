package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.lastmile.drop.draft.SeasonalDropAudit
import com.acme.transport.lastmile.drop.draft.normalizeSeasonalDropAuditCode

fun summarizeSeasonalDropAuditsForLedger(items: MutableList<SeasonalDropAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveGlobalRedeliveryLedgersFromAudit(items: MutableList<SeasonalDropAudit>): MutableList<GlobalRedeliveryLedger> {
    val out = mutableListOf<GlobalRedeliveryLedger>()
    for (item in items) {
        val derived = newGlobalRedeliveryLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateGlobalRedeliveryLedger(item: GlobalRedeliveryLedger): String {
    return normalizeSeasonalDropAuditCode(item.code)
}
