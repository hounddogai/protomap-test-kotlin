package com.acme.transport.crewing.roster.digest

import com.acme.transport.crewing.restbreak.exception.AdaptiveRestBreakScore
import com.acme.transport.crewing.restbreak.exception.restBreakExceptionBucket

fun summarizeAdaptiveRestBreakScoresForDigest(items: MutableList<AdaptiveRestBreakScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRosterDigestsFromScore(items: MutableList<AdaptiveRestBreakScore>): MutableList<RosterDigest> {
    val out = mutableListOf<RosterDigest>()
    for (item in items) {
        val derived = newRosterDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRosterDigest(item: RosterDigest): String = restBreakExceptionBucket(item.effectiveValue())
