package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.linehaul.tender.window.AdaptiveTenderMilestone
import com.acme.transport.linehaul.tender.window.rollingTenderManifestBucket

fun summarizeAdaptiveTenderMilestonesForThreshold(items: MutableList<AdaptiveTenderMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTieredLanePlanThresholdsFromMilestone(items: MutableList<AdaptiveTenderMilestone>): MutableList<TieredLanePlanThreshold> {
    val out = mutableListOf<TieredLanePlanThreshold>()
    for (item in items) {
        val derived = newTieredLanePlanThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankTieredLanePlanThreshold(item: TieredLanePlanThreshold): String {
    return rollingTenderManifestBucket(item.projectedExposure())
}
