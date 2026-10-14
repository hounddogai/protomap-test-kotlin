package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.linehaul.laneplan.review.LegacyLanePlanSlot
import com.acme.transport.linehaul.laneplan.review.normalizeRollingLanePlanReviewCode

fun summarizeLegacyLanePlanSlotsForThreshold(items: MutableList<LegacyLanePlanSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTieredLanePlanThresholdsFromSlot(items: MutableList<LegacyLanePlanSlot>): MutableList<TieredLanePlanThreshold> {
    val out = mutableListOf<TieredLanePlanThreshold>()
    for (item in items) {
        val derived = newTieredLanePlanThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTieredLanePlanThreshold(item: TieredLanePlanThreshold): String {
    return normalizeRollingLanePlanReviewCode(item.code)
}
