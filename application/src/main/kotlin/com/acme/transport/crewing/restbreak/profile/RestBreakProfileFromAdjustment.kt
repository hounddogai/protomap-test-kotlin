package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.roster.cycle.RosterAdjustment
import com.acme.transport.crewing.roster.cycle.rosterTargetBucket

fun summarizeRosterAdjustmentsForProfile(items: MutableList<RosterAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRestBreakProfilesFromAdjustment(items: MutableList<RosterAdjustment>): MutableList<RestBreakProfile> {
    val out = mutableListOf<RestBreakProfile>()
    for (item in items) {
        val derived = newRestBreakProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeRestBreakProfile(item: RestBreakProfile): String = rosterTargetBucket(item.effectiveValue())
