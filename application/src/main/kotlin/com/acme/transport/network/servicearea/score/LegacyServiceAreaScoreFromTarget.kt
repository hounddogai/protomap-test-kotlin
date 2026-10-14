package com.acme.transport.network.servicearea.score

import com.acme.transport.network.servicearea.areatarget.AdaptiveServiceAreaTarget
import com.acme.transport.network.servicearea.areatarget.strictServiceAreaAdjustmentBucket

fun summarizeAdaptiveServiceAreaTargetsForScore(items: MutableList<AdaptiveServiceAreaTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyServiceAreaScoresFromTarget(items: MutableList<AdaptiveServiceAreaTarget>): MutableList<LegacyServiceAreaScore> {
    val out = mutableListOf<LegacyServiceAreaScore>()
    for (item in items) {
        val derived = newLegacyServiceAreaScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLegacyServiceAreaScore(item: LegacyServiceAreaScore): String {
    return strictServiceAreaAdjustmentBucket(item.weightedExposure())
}
