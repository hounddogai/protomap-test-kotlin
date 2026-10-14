package com.acme.transport.network.cutoff.preference

import com.acme.transport.network.cutoff.profile.CutoffTier
import com.acme.transport.network.cutoff.profile.cutoffPlanBucket

fun summarizeCutoffTiersForWindow(items: MutableList<CutoffTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveCutoffWindowsFromTier(items: MutableList<CutoffTier>): MutableList<CutoffWindow> {
    val out = mutableListOf<CutoffWindow>()
    for (item in items) {
        val derived = newCutoffWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCutoffWindow(item: CutoffWindow): String = cutoffPlanBucket(item.measuredValue())
