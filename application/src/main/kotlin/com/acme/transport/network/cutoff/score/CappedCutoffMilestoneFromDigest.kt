package com.acme.transport.network.cutoff.score

import com.acme.transport.network.cutoff.slot.CutoffDigest
import com.acme.transport.network.cutoff.slot.cutoffDigestBucket

fun summarizeCutoffDigestsForMilestone(items: MutableList<CutoffDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedCutoffMilestonesFromDigest(items: MutableList<CutoffDigest>): MutableList<CappedCutoffMilestone> {
    val out = mutableListOf<CappedCutoffMilestone>()
    for (item in items) {
        val derived = newCappedCutoffMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planCappedCutoffMilestone(item: CappedCutoffMilestone): String = cutoffDigestBucket(item.effectiveLoad())
