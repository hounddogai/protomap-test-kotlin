package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.linehaul.backhaul.rollup.PrimaryBackhaulScore
import com.acme.transport.linehaul.backhaul.rollup.classifyPrimaryBackhaulRollup

fun summarizePrimaryBackhaulScoresForBaseline(items: MutableList<PrimaryBackhaulScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAdaptiveTrailerBaselinesFromScore(items: MutableList<PrimaryBackhaulScore>): MutableList<AdaptiveTrailerBaseline> {
    val out = mutableListOf<AdaptiveTrailerBaseline>()
    for (item in items) {
        val derived = newAdaptiveTrailerBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveTrailerBaseline(item: AdaptiveTrailerBaseline): String {
    return classifyPrimaryBackhaulRollup(item.projectedVolume())
}
