package com.acme.transport.network.servicearea.request

import com.acme.transport.network.cutoff.preference.CutoffWindow
import com.acme.transport.network.cutoff.preference.normalizePendingCutoffScoreCode

fun summarizeCutoffWindowsForCheckpoint(items: MutableList<CutoffWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveServiceAreaCheckpointsFromWindow(items: MutableList<CutoffWindow>): MutableList<AdaptiveServiceAreaCheckpoint> {
    val out = mutableListOf<AdaptiveServiceAreaCheckpoint>()
    for (item in items) {
        val derived = newAdaptiveServiceAreaCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveServiceAreaCheckpoint(item: AdaptiveServiceAreaCheckpoint): String {
    return normalizePendingCutoffScoreCode(item.code)
}
