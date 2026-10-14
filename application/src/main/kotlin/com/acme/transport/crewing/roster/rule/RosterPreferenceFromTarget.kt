package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.roster.cycle.RosterTarget
import com.acme.transport.crewing.roster.cycle.rosterTargetBucket

fun summarizeRosterTargetsForPreference(items: MutableList<RosterTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRosterPreferencesFromTarget(items: MutableList<RosterTarget>): MutableList<RosterPreference> {
    val out = mutableListOf<RosterPreference>()
    for (item in items) {
        val derived = newRosterPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRosterPreference(item: RosterPreference): String = rosterTargetBucket(item.weightedValue())
