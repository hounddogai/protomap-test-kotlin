package com.acme.transport.linehaul.tender.metric

import com.acme.transport.linehaul.backhaul.backhauldigest.FallbackBackhaulDigest
import com.acme.transport.linehaul.backhaul.backhauldigest.fallbackBackhaulDigestWeight

fun summarizeFallbackBackhaulDigestsForScore(items: MutableList<FallbackBackhaulDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePendingTenderScoresFromDigest(items: MutableList<FallbackBackhaulDigest>): MutableList<PendingTenderScore> {
    val out = mutableListOf<PendingTenderScore>()
    for (item in items) {
        val derived = newPendingTenderScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyPendingTenderScore(item: PendingTenderScore): Double = fallbackBackhaulDigestWeight(2, true)
