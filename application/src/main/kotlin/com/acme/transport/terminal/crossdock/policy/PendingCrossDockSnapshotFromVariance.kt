package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.terminal.dwell.adjustment.WeightedDwellVariance
import com.acme.transport.terminal.dwell.adjustment.weightedDwellVarianceWeight

fun summarizeWeightedDwellVariancesForSnapshot(items: MutableList<WeightedDwellVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePendingCrossDockSnapshotsFromVariance(items: MutableList<WeightedDwellVariance>): MutableList<PendingCrossDockSnapshot> {
    val out = mutableListOf<PendingCrossDockSnapshot>()
    for (item in items) {
        val derived = newPendingCrossDockSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolvePendingCrossDockSnapshot(item: PendingCrossDockSnapshot): Double = weightedDwellVarianceWeight(6, true)
