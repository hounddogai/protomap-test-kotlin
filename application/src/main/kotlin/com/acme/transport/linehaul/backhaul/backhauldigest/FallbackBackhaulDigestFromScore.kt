package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.linehaul.laneplan.planrule.LanePlanScore
import com.acme.transport.linehaul.laneplan.planrule.lanePlanProposalBucket

fun summarizeLanePlanScoresForDigest(items: MutableList<LanePlanScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackBackhaulDigestsFromScore(items: MutableList<LanePlanScore>): MutableList<FallbackBackhaulDigest> {
    val out = mutableListOf<FallbackBackhaulDigest>()
    for (item in items) {
        val derived = newFallbackBackhaulDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackBackhaulDigest(item: FallbackBackhaulDigest): String = lanePlanProposalBucket(item.netExposure())
