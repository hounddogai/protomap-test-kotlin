package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.lastmile.attempt.overrideitem.PrimaryAttemptDigest
import com.acme.transport.lastmile.attempt.overrideitem.classifyRegionalAttemptRule

fun summarizePrimaryAttemptDigestsForNotice(items: MutableList<PrimaryAttemptDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackDropNoticesFromDigest(items: MutableList<PrimaryAttemptDigest>): MutableList<FallbackDropNotice> {
    val out = mutableListOf<FallbackDropNotice>()
    for (item in items) {
        val derived = newFallbackDropNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeFallbackDropNotice(item: FallbackDropNotice): String {
    return classifyRegionalAttemptRule(item.adjustedVolume())
}
