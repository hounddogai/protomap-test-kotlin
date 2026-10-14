package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.terminal.crossdock.template.CrossDockThreshold
import com.acme.transport.terminal.crossdock.template.crossDockThresholdBucket

fun summarizeCrossDockThresholdsForPolicy(items: MutableList<CrossDockThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSeasonalDwellPoliciesFromThreshold(items: MutableList<CrossDockThreshold>): MutableList<SeasonalDwellPolicy> {
    val out = mutableListOf<SeasonalDwellPolicy>()
    for (item in items) {
        val derived = newSeasonalDwellPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankSeasonalDwellPolicy(item: SeasonalDwellPolicy): String = crossDockThresholdBucket(item.netTotal())
