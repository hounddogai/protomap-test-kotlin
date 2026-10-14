package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.linehaul.laneplan.planbundle.LanePlanLedger
import com.acme.transport.linehaul.laneplan.planbundle.lanePlanReviewBucket

fun summarizeLanePlanLedgersForBundle(items: MutableList<LanePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveLanePlanBundlesFromLedger(items: MutableList<LanePlanLedger>): MutableList<AdaptiveLanePlanBundle> {
    val out = mutableListOf<AdaptiveLanePlanBundle>()
    for (item in items) {
        val derived = newAdaptiveLanePlanBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveLanePlanBundle(item: AdaptiveLanePlanBundle): String {
    return lanePlanReviewBucket(item.effectiveTotal())
}
