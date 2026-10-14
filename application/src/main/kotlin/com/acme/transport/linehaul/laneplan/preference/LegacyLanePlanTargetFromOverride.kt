package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.linehaul.trailer.traileroverride.BatchedTrailerOverride
import com.acme.transport.linehaul.trailer.traileroverride.tieredTrailerWindowWeight

fun summarizeBatchedTrailerOverridesForTarget(items: MutableList<BatchedTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLegacyLanePlanTargetsFromOverride(items: MutableList<BatchedTrailerOverride>): MutableList<LegacyLanePlanTarget> {
    val out = mutableListOf<LegacyLanePlanTarget>()
    for (item in items) {
        val derived = newLegacyLanePlanTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLegacyLanePlanTarget(item: LegacyLanePlanTarget): Double = tieredTrailerWindowWeight(3, true)
