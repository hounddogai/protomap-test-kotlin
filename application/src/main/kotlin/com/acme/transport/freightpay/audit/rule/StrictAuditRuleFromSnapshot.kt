package com.acme.transport.freightpay.audit.rule

import com.acme.transport.freightpay.ratecard.regionalcardsnapshot.RegionalRateCardSnapshot
import com.acme.transport.freightpay.ratecard.regionalcardsnapshot.regionalRateCardSnapshotWeight

fun summarizeRegionalRateCardSnapshotsForRule(items: MutableList<RegionalRateCardSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictAuditRulesFromSnapshot(items: MutableList<RegionalRateCardSnapshot>): MutableList<StrictAuditRule> {
    val out = mutableListOf<StrictAuditRule>()
    for (item in items) {
        val derived = newStrictAuditRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateStrictAuditRule(item: StrictAuditRule): Double = regionalRateCardSnapshotWeight(4, true)
