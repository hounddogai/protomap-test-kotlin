package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.swap.digest.SwapDigest
import com.acme.transport.crewing.swap.digest.rollingSwapSnapshotBucket

fun summarizeSwapDigestsForRule(items: MutableList<SwapDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDefaultRosterRulesFromDigest(items: MutableList<SwapDigest>): MutableList<DefaultRosterRule> {
    val out = mutableListOf<DefaultRosterRule>()
    for (item in items) {
        val derived = newDefaultRosterRule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultRosterRule(item: DefaultRosterRule): String = rollingSwapSnapshotBucket(item.netValue())
