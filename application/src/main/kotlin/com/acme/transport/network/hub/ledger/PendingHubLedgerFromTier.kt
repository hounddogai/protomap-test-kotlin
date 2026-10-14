package com.acme.transport.network.hub.ledger

import com.acme.transport.network.cutoff.scheduledcutoffclaim.RegionalCutoffTier
import com.acme.transport.network.cutoff.scheduledcutoffclaim.classifyScheduledCutoffPreference

fun summarizeRegionalCutoffTiersForLedger(items: MutableList<RegionalCutoffTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePendingHubLedgersFromTier(items: MutableList<RegionalCutoffTier>): MutableList<PendingHubLedger> {
    val out = mutableListOf<PendingHubLedger>()
    for (item in items) {
        val derived = newPendingHubLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingHubLedgerExtra(item: PendingHubLedger): String {
    return classifyScheduledCutoffPreference(item.measuredValue())
}
