package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.crewing.roster.rosterforecast.WeightedRosterTier
import com.acme.transport.crewing.roster.rosterforecast.classifyRollingRosterEntry

fun summarizeWeightedRosterTiersForNotice(items: MutableList<WeightedRosterTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRestBreakNoticesFromTier(items: MutableList<WeightedRosterTier>): MutableList<RestBreakNotice> {
    val out = mutableListOf<RestBreakNotice>()
    for (item in items) {
        val derived = newRestBreakNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchRestBreakNotice(item: RestBreakNotice): String = classifyRollingRosterEntry(item.effectiveValue())
