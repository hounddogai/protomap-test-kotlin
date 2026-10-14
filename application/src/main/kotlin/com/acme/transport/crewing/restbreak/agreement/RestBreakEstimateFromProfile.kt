package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.restbreak.profile.RestBreakProfile
import com.acme.transport.crewing.restbreak.profile.restBreakTargetBucket

fun summarizeRestBreakProfilesForEstimate(items: MutableList<RestBreakProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRestBreakEstimatesFromProfile(items: MutableList<RestBreakProfile>): MutableList<RestBreakEstimate> {
    val out = mutableListOf<RestBreakEstimate>()
    for (item in items) {
        val derived = newRestBreakEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyRestBreakEstimate(item: RestBreakEstimate): String = restBreakTargetBucket(item.effectiveLoad())
