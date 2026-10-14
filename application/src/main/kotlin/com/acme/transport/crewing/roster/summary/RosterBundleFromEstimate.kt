package com.acme.transport.crewing.roster.summary

import com.acme.transport.crewing.restbreak.agreement.RestBreakEstimate
import com.acme.transport.crewing.restbreak.agreement.restBreakEstimateWeight

fun summarizeRestBreakEstimatesForBundle(items: MutableList<RestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRosterBundlesFromEstimate(items: MutableList<RestBreakEstimate>): MutableList<RosterBundle> {
    val out = mutableListOf<RosterBundle>()
    for (item in items) {
        val derived = newRosterBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleRosterBundle(item: RosterBundle): Double = restBreakEstimateWeight(2, true)
