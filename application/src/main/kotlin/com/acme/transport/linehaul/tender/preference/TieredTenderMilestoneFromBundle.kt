package com.acme.transport.linehaul.tender.preference

import com.acme.transport.linehaul.laneplan.summary.BatchedLanePlanBundle
import com.acme.transport.linehaul.laneplan.summary.normalizeCappedLanePlanExceptionCode

fun summarizeBatchedLanePlanBundlesForMilestone(items: MutableList<BatchedLanePlanBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTieredTenderMilestonesFromBundle(items: MutableList<BatchedLanePlanBundle>): MutableList<TieredTenderMilestone> {
    val out = mutableListOf<TieredTenderMilestone>()
    for (item in items) {
        val derived = newTieredTenderMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveTieredTenderMilestone(item: TieredTenderMilestone): String {
    return normalizeCappedLanePlanExceptionCode(item.code)
}
