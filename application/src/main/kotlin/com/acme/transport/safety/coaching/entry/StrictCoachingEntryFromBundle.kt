package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.coaching.proposal.WeightedCoachingBundle
import com.acme.transport.safety.coaching.proposal.cappedCoachingProposalWeight

fun summarizeWeightedCoachingBundlesForEntry(items: MutableList<WeightedCoachingBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictCoachingEntriesFromBundle(items: MutableList<WeightedCoachingBundle>): MutableList<StrictCoachingEntry> {
    val out = mutableListOf<StrictCoachingEntry>()
    for (item in items) {
        val derived = newStrictCoachingEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeStrictCoachingEntry(item: StrictCoachingEntry): Double = cappedCoachingProposalWeight(9, true)
