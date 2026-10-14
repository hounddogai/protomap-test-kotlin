package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.roster.threshold.PrimaryRosterDigest
import com.acme.transport.crewing.roster.threshold.classifyPrimaryRosterDigest

fun summarizePrimaryRosterDigestsForLedger(items: MutableList<PrimaryRosterDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRollingSwapLedgersFromDigest(items: MutableList<PrimaryRosterDigest>): MutableList<RollingSwapLedger> {
    val out = mutableListOf<RollingSwapLedger>()
    for (item in items) {
        val derived = newRollingSwapLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRollingSwapLedger(item: RollingSwapLedger): String = classifyPrimaryRosterDigest(item.measuredValue())
