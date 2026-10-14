package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.network.transittime.rollup.LegacyTransitTimeRollup
import com.acme.transport.network.transittime.rollup.scheduledTransitTimeManifestWeight

fun summarizeLegacyTransitTimeRollupsForRecord(items: MutableList<LegacyTransitTimeRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCutoffRecordsFromRollup(items: MutableList<LegacyTransitTimeRollup>): MutableList<CutoffRecord> {
    val out = mutableListOf<CutoffRecord>()
    for (item in items) {
        val derived = newCutoffRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateCutoffRecord(item: CutoffRecord): Double = scheduledTransitTimeManifestWeight(6, true)
